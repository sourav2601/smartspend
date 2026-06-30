package com.smartspend.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspend.backend.dto.GoalDtos;
import com.smartspend.backend.entity.Expense;
import com.smartspend.backend.entity.Goal;
import com.smartspend.backend.entity.User;
import com.smartspend.backend.exception.ResourceNotFoundException;
import com.smartspend.backend.repository.ExpenseRepository;
import com.smartspend.backend.repository.GoalRepository;
import com.smartspend.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The core "smart" feature of the app: given a user's real spending
 * history and a goal (e.g. "save Rs 70,000 for an iPhone in 3 months"),
 * produce a specific, actionable savings plan.
 *
 * Design choice worth explaining in a report/interview: this service
 * does the *numeric* work itself (averaging spend per category, basic
 * arithmetic) and only hands Claude a clean, pre-computed summary to
 * turn into a structured plan + natural-language explanation. This
 * avoids relying on an LLM to do arithmetic it might get wrong, and
 * keeps the AI's job to what it's actually good at: reasoning over
 * already-correct numbers and writing it up clearly.
 */
@Service
public class GoalService {

    private static final Logger log = LoggerFactory.getLogger(GoalService.class);

    private static final int LOOKBACK_MONTHS = 3;

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final AnthropicClient anthropicClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GoalService(
            GoalRepository goalRepository,
            UserRepository userRepository,
            ExpenseRepository expenseRepository,
            AnthropicClient anthropicClient
    ) {
        this.goalRepository = goalRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.anthropicClient = anthropicClient;
    }

    @Transactional
    public GoalDtos.GoalResponse createGoalWithPlan(Long userId, GoalDtos.CreateGoalRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Goal goal = new Goal();
        goal.setOwner(owner);
        goal.setTitle(request.title());
        goal.setTargetAmount(request.targetAmount());
        goal.setTargetDate(request.targetDate());
        goalRepository.save(goal);

        Map<String, Object> plan = generatePlan(userId, goal);
        goal.setPlan(plan);
        goal.setUpdatedAt(Instant.now());

        return GoalDtos.GoalResponse.from(goal);
    }

    @Transactional
    public GoalDtos.GoalResponse regeneratePlan(Long userId, Long goalId) {
        Goal goal = goalRepository.findByIdAndOwnerId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        Map<String, Object> plan = generatePlan(userId, goal);
        goal.setPlan(plan);
        goal.setUpdatedAt(Instant.now());

        return GoalDtos.GoalResponse.from(goal);
    }

    public List<GoalDtos.GoalResponse> listGoals(Long userId) {
        return goalRepository.findByOwnerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(GoalDtos.GoalResponse::from)
                .toList();
    }

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        Goal goal = goalRepository.findByIdAndOwnerId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        goalRepository.delete(goal);
    }

    // --- Core plan generation logic ---

    private Map<String, Object> generatePlan(Long userId, Goal goal) {
        Map<String, Double> avgMonthlySpendByCategory = computeAverageMonthlySpend(userId);
        double avgTotalMonthlySpend = avgMonthlySpendByCategory.values().stream()
                .mapToDouble(Double::doubleValue).sum();

        // Feasibility check, done with our own arithmetic before ever
        // calling the AI: if hitting the deadline would require saving
        // more than 1/3 of total average monthly spend, that's not a
        // realistic ask - asking the AI to "try harder" would only
        // produce an unrealistic plan (e.g. cutting essentials to zero).
        // Instead, tell the user directly and suggest the earliest
        // deadline that IS achievable at the 1/3 threshold.
        Map<String, Object> infeasiblePlan = checkDeadlineFeasibility(goal, avgTotalMonthlySpend);
        if (infeasiblePlan != null) {
            return infeasiblePlan;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        double monthlyIncome = user.getMonthlyIncome() != null ? user.getMonthlyIncome() : 0.0;

        String systemPrompt = """
                You are a financial planning assistant inside an expense tracker app.
                You will be given a user's real average monthly spending by category,
                their monthly income (may be 0 if unknown), and a savings goal.

                Respond with ONLY a valid JSON object (no markdown fences, no preamble),
                with this exact shape:
                {
                  "cuts": [{"category": "string", "currentMonthly": number, "reduceBy": number, "newMonthly": number, "reason": "reason in 8 words or fewer"}],
                  "newMonthlySavings": number,
                  "projectedCompletionDate": "YYYY-MM-DD",
                  "summary": "1-2 sentence plain language summary of the plan",
                  "hook": "1-2 short, exciting, emoji-friendly lines about THIS specific goal item"
                }

                Rules:
                - "hook" should be fun, personal, and specific to the goal title (e.g. for "iPhone 17" reference the actual product/excitement of owning it, not generic savings talk) - this is the one field allowed to have personality and emojis, 1-2 short lines max.
                - If a required monthly savings amount is given to hit a deadline, treat it as the target to aim for - prefer a plan that reaches or gets close to that number over a smaller, more comfortable cut, as long as it stays realistic (don't cut a category to zero unless it's genuinely discretionary).
                - The "cuts" array MUST include exactly one entry for EVERY category listed in "Spending by category" below - do not omit any category, even if you suggest no change for it (in that case, set reduceBy to 0 and newMonthly equal to currentMonthly, with a brief reason like "Already efficient, no change needed").
                - Be specific and realistic - do not suggest cutting a category to zero unless it's clearly discretionary.
                - Keep every "reason" and the "summary" short and to the point - brevity matters more than detail here.
                - Do NOT mention or estimate any specific date or "by [month/year]" claim in "summary" or "hook" - only describe the spending cuts and the resulting monthly savings amount. The completion date is calculated separately and shown elsewhere.
                - Base "reason" on the actual numbers given, not generic advice.
                - If income is 0 or not provided, base the plan only on reducing existing spend, not on "save X% of income".
                - Do not include any text outside the JSON object.
                """;

        String userPrompt = buildUserPrompt(goal, avgMonthlySpendByCategory, avgTotalMonthlySpend, monthlyIncome);

        String rawJson;
        try {
            rawJson = anthropicClient.complete(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.error("Gemini API call itself failed (network/auth/timeout)", e);
            return buildFallbackPlan(avgMonthlySpendByCategory);
        }

        log.info("Raw Gemini response for goal '{}': {}", goal.getTitle(), rawJson);
        return parsePlanJson(rawJson, avgMonthlySpendByCategory, goal);
    }

    /**
     * Deterministic, pre-AI feasibility check: if the monthly savings
     * required to hit the goal's deadline would exceed 1/3 of the
     * user's total average monthly spend, treat the deadline itself as
     * unrealistic rather than asking the AI to find cuts that would
     * require gutting essential categories. Returns a plan explaining
     * this and stating the earliest deadline that IS achievable at the
     * 1/3 threshold, or null if the deadline is feasible (in which case
     * the caller proceeds to call the AI normally).
     */
    private Map<String, Object> checkDeadlineFeasibility(Goal goal, double avgTotalMonthlySpend) {
        if (goal.getTargetDate() == null || avgTotalMonthlySpend <= 0) {
            // No deadline given, or no spending history yet to judge
            // feasibility against - nothing to check, proceed normally.
            return null;
        }

        long monthsUntilDeadline = java.time.temporal.ChronoUnit.MONTHS.between(
                LocalDate.now(), goal.getTargetDate());
        double remaining = goal.getTargetAmount() - goal.getCurrentSaved();

        if (remaining <= 0 || monthsUntilDeadline <= 0) {
            // Goal already met, or deadline already passed - not a
            // feasibility question, let the normal flow handle it.
            return null;
        }

        double requiredMonthly = remaining / monthsUntilDeadline;
        double feasibilityThreshold = avgTotalMonthlySpend / 3.0;

        if (requiredMonthly <= feasibilityThreshold) {
            return null; // Feasible - proceed to call the AI as normal.
        }

        // Infeasible: compute the earliest deadline that WOULD work,
        // saving at most 1/3 of total monthly spend.
        long minMonthsNeeded = (long) Math.ceil(remaining / feasibilityThreshold);
        LocalDate minFeasibleDate = LocalDate.now().plusMonths(minMonthsNeeded);

        return Map.of(
                "cuts", List.<Object>of(),
                "newMonthlySavings", 0,
                "projectedCompletionDate", "",
                "infeasible", true,
                "requiredMonthly", requiredMonthly,
                "minFeasibleDate", minFeasibleDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                "summary", "Hitting this deadline would need Rs " + String.format("%.0f", requiredMonthly)
                        + "/month - more than a third of your average spending. Try a later date, "
                        + "such as " + minFeasibleDate.format(DateTimeFormatter.ofPattern("d MMM yyyy")) + ".",
                "hook", "This timeline's a stretch! \uD83D\uDCC5 Push the date out a bit and it's totally doable."
        );
    }

    private String buildUserPrompt(
            Goal goal,
            Map<String, Double> avgMonthlySpendByCategory,
            double avgTotalMonthlySpend,
            double monthlyIncome
    ) {
        StringBuilder spendLines = new StringBuilder();
        avgMonthlySpendByCategory.forEach((category, amount) ->
                spendLines.append("- ").append(category).append(": Rs ")
                        .append(String.format("%.0f", amount)).append("/month\n"));

        String targetDateStr = goal.getTargetDate() != null
                ? goal.getTargetDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : "no fixed deadline - aim for as soon as reasonably possible";

        // Compute the monthly savings rate actually required to hit the
        // user's stated deadline, so the AI has a concrete number to aim
        // for rather than picking comfortable cuts and letting the date
        // drift. This number is also reused as-is if the deadline turns
        // out to be mathematically impossible (see requiredMonthlyNote).
        String requiredMonthlyNote;
        if (goal.getTargetDate() != null) {
            long monthsUntilDeadline = java.time.temporal.ChronoUnit.MONTHS.between(
                    LocalDate.now(), goal.getTargetDate());
            double remaining = goal.getTargetAmount() - goal.getCurrentSaved();

            if (monthsUntilDeadline <= 0) {
                requiredMonthlyNote = "The target date has already passed or is this month - "
                        + "save as aggressively as realistically possible.";
            } else {
                double requiredMonthly = remaining / monthsUntilDeadline;
                requiredMonthlyNote = "To hit the target date, the user needs to save approximately Rs "
                        + String.format("%.0f", requiredMonthly) + "/month (over " + monthsUntilDeadline
                        + " months). Prioritize cuts that get newMonthlySavings as close to this number as "
                        + "realistically possible - do not settle for a small, comfortable cut if a larger "
                        + "one is needed and plausible given the spending shown below.";
            }
        } else {
            requiredMonthlyNote = "No fixed deadline was given - aim for a reasonable, sustainable savings rate.";
        }

        return """
                Goal: %s
                Target amount: Rs %.0f
                Target date: %s
                Already saved toward this goal: Rs %.0f
                %s

                Monthly income: Rs %.0f (0 means unknown)
                Average total monthly spending (last %d months): Rs %.0f

                Spending by category (monthly average):
                %s

                Generate the savings plan JSON as instructed.
                """.formatted(
                goal.getTitle(), goal.getTargetAmount(), targetDateStr, goal.getCurrentSaved(),
                requiredMonthlyNote,
                monthlyIncome, LOOKBACK_MONTHS, avgTotalMonthlySpend,
                spendLines.toString()
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parsePlanJson(String rawJson, Map<String, Double> avgMonthlySpendByCategory, Goal goal) {
        try {
            String cleaned = stripMarkdownFences(rawJson);
            cleaned = repairCommonJsonMistakes(cleaned);
            Map<String, Object> plan = (Map<String, Object>) objectMapper.readValue(cleaned, Map.class);
            plan = backfillMissingCategories(plan, avgMonthlySpendByCategory);
            return correctProjectedDate(plan, goal);
        } catch (Exception e) {
            // If the AI response can't be parsed, fall back to a minimal
            // plan derived purely from our own numbers, so the feature
            // degrades gracefully instead of throwing a 500 at the user.
            // Logged at ERROR with full stack trace so the real cause
            // (bad API key, network failure, unexpected response shape,
            // etc.) is visible in the console instead of silently hidden.
            log.error("Failed to get/parse AI savings plan from Gemini. Raw response was: {}", rawJson, e);
            return buildFallbackPlan(avgMonthlySpendByCategory);
        }
    }

    /**
     * The AI is instructed to include every spending category in its
     * "cuts" list, but - observed in testing - it sometimes omits
     * categories anyway (likely judging them not worth mentioning).
     * Rather than rely on the AI remembering this instruction every
     * time, we check its output here and add a "no change suggested"
     * entry for any category it left out, using our own real spending
     * numbers. This guarantees the UI always shows the user's complete
     * spending picture, regardless of what the AI chose to comment on.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> backfillMissingCategories(
            Map<String, Object> plan, Map<String, Double> avgMonthlySpendByCategory
    ) {
        Object cutsObj = plan.get("cuts");
        List<Map<String, Object>> cuts = (cutsObj instanceof List<?> l)
                ? new java.util.ArrayList<>((List<Map<String, Object>>) l)
                : new java.util.ArrayList<>();

        Set<String> categoriesAlreadyCovered = cuts.stream()
                .map(cut -> String.valueOf(cut.get("category")))
                .collect(Collectors.toSet());

        for (Map.Entry<String, Double> entry : avgMonthlySpendByCategory.entrySet()) {
            String category = entry.getKey();
            if (categoriesAlreadyCovered.contains(category)) continue;

            double currentMonthly = entry.getValue();
            cuts.add(Map.of(
                    "category", category,
                    "currentMonthly", currentMonthly,
                    "reduceBy", 0,
                    "newMonthly", currentMonthly,
                    "reason", "No change suggested for this category."
            ));
        }

        plan.put("cuts", cuts);
        return plan;
    }

    /**
     * The AI is good at suggesting *what* to cut, but date arithmetic
     * (given a monthly savings rate, when will the goal be reached?) is
     * exactly the kind of calculation LLMs can get wrong even when the
     * rest of the response is sound - observed in testing, where the
     * model echoed back the user's originally-requested target date
     * instead of actually computing months-needed from the savings rate.
     *
     * Rather than trust the AI's "projectedCompletionDate" field, we
     * recompute it ourselves here from numbers we already trust: the
     * goal's remaining amount and the AI-suggested monthly savings rate.
     */
    private Map<String, Object> correctProjectedDate(Map<String, Object> plan, Goal goal) {
        double remaining = goal.getTargetAmount() - goal.getCurrentSaved();
        double monthlySavings = toDouble(plan.get("newMonthlySavings"));

        String correctedDate;
        if (monthlySavings <= 0 || remaining <= 0) {
            // No savings rate to work with, or goal already met - leave
            // as "unknown" rather than divide by zero or claim "now".
            correctedDate = remaining <= 0 ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
        } else {
            long monthsNeeded = (long) Math.ceil(remaining / monthlySavings);
            correctedDate = LocalDate.now().plusMonths(monthsNeeded).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        plan.put("projectedCompletionDate", correctedDate);
        return plan;
    }

    private double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
                return 0.0;
            }
        }
        return 0.0;
    }

    /**
     * Strips markdown code fences (```json ... ```) that models sometimes
     * wrap JSON in despite being told not to.
     */
    private String stripMarkdownFences(String rawJson) {
        String cleaned = rawJson.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("^```(json)?", "").replaceAll("```$", "").trim();
        }
        return cleaned;
    }

    /**
     * LLMs generating long, deeply-nested JSON occasionally make small
     * punctuation mistakes - a stray comma before a closing brace, or a
     * missing comma between array elements. Rather than failing the
     * whole request over a single misplaced character, we repair the
     * most common patterns observed in practice from real Gemini
     * responses during testing:
     *
     *   1. ",}" or ",]"  -> trailing comma directly before a closing bracket
     *   2. "}\n{" or "}\n\"key\"" with no comma between them -> missing
     *      comma between adjacent array elements or object fields
     *
     * This is a pragmatic, observed-failure-driven fix, not a full JSON
     * grammar repair - if the response is malformed in some other way,
     * we still fall through to the fallback plan rather than risk
     * silently producing wrong data.
     */
    private String repairCommonJsonMistakes(String json) {
        String repaired = json;
        // A stray trailing comma directly before a closing bracket.
        repaired = repaired.replaceAll(",\\s*([}\\]])", "$1");
        // A comma on its own line immediately followed by '{' with no
        // closing brace before it (a missing '}' was omitted before
        // starting the next array element). The negative lookbehind
        // ensures we don't touch already-correct "},\n{" boundaries -
        // only fire when there is NOT already a '}' right before the comma.
        repaired = repaired.replaceAll("(?<!\\})\\s*,\\s*\\n\\s*\\{", "\n},\n{");
        return repaired;
    }

    private Map<String, Object> buildFallbackPlan(Map<String, Double> avgMonthlySpendByCategory) {
        List<Map<String, Object>> cuts = avgMonthlySpendByCategory.entrySet().stream()
                .map(e -> Map.<String, Object>of(
                        "category", e.getKey(),
                        "currentMonthly", e.getValue(),
                        "reduceBy", 0,
                        "newMonthly", e.getValue(),
                        "reason", "Unable to generate a detailed plan right now - showing current spend only."
                ))
                .collect(Collectors.toList());

        return Map.of(
                "cuts", cuts,
                "newMonthlySavings", 0,
                "projectedCompletionDate", "",
                "summary", "We couldn't generate a detailed AI plan right now. Please try regenerating the plan.",
                "hook", "Your goal is waiting for you! 🎯"
        );
    }

    /**
     * Computes average monthly spend per category over the lookback
     * window, using in-memory grouping (see ExpenseRepository comment
     * for why this isn't done as a SQL subquery).
     */
    private Map<String, Double> computeAverageMonthlySpend(Long userId) {
        LocalDate since = LocalDate.now().minusMonths(LOOKBACK_MONTHS);
        List<Expense> expenses = expenseRepository.findByOwnerIdAndDateGreaterThanEqual(userId, since);

        // Group by category name, sum amounts, then divide by lookback months
        // to get a monthly average per category.
        Map<String, Double> totalByCategory = expenses.stream()
                .filter(e -> e.getCategory() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.summingDouble(Expense::getAmount)
                ));

        return totalByCategory.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() / LOOKBACK_MONTHS
                ));
    }
}