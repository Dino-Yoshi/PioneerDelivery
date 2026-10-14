# Task D: When a Greedy Choice Fails

## Problem setup

Each delivery takes one unit of driver time. Delivery j has a nonnegative integer ready time R\_j and a positive profit P\_j. The profits are pairwise distinct.

A valid schedule S assigns each delivery to a distinct time slot T\_j(S) at or after its ready time R\_j. Its delay and value are:

```
D_j(S) = T_j(S) - R_j
V_j(S) = P_j / (1 + D_j(S))
```

The objective is to maximize V(S), the sum of all delivery values. The proposed greedy strategy G considers deliveries in decreasing profit order and assigns each one the earliest unoccupied slot at or after its ready time.

## Counterexample

| Profit P\_j | Ready time R\_j |
| -: | -: |
| 8 | 5 |
| 7 | 1 |
| 3 | 3 |
| 4 | 2 |
| 9 | 1 |


All profits are positive and distinct, and all ready times are nonnegative integers.

### Schedule produced by G

The rows below follow the order in which the greedy strategy **processes** deliveries, not chronological slot order. In particular, it assigns the profit-8 delivery to slot 5 before considering the profit-7 delivery for slot 2.

| Profit | Ready time | Assigned slot | Delay | Value |
| -: | -: | -: | -: | -: |
| 9 | 1 | 1 | 1 - 1 = 0 | 9 / (1 + 0) = 9 |
| 8 | 5 | 5 | 5 - 5 = 0 | 8 / (1 + 0) = 8 |
| 7 | 1 | 2 | 2 - 1 = 1 | 7 / (1 + 1) = 3.5 |
| 4 | 2 | 3 | 3 - 2 = 1 | 4 / (1 + 1) = 2 |
| 3 | 3 | 4 | 4 - 3 = 1 | 3 / (1 + 1) = 1.5 |


Thus, V(G) = 9 + 8 + 3.5 + 2 + 1.5 = 24.

### A better schedule S

| Profit | Ready time | Assigned slot | Delay | Value |
| -: | -: | -: | -: | -: |
| 9 | 1 | 1 | 1 - 1 = 0 | 9 / (1 + 0) = 9 |
| 4 | 2 | 2 | 2 - 2 = 0 | 4 / (1 + 0) = 4 |
| 3 | 3 | 3 | 3 - 3 = 0 | 3 / (1 + 0) = 3 |
| 7 | 1 | 4 | 4 - 1 = 3 | 7 / (1 + 3) = 1.75 |
| 8 | 5 | 5 | 5 - 5 = 0 | 8 / (1 + 0) = 8 |


Every delivery is scheduled at or after its ready time, and no two deliveries share a slot. Thus, S is valid and V(S) = 9 + 4 + 3 + 1.75 + 8 = 25.75.

Since V(S) - V(G) = 25.75 - 24 = 1.75 \> 0, this counterexample shows that the manager's greedy strategy is not guaranteed to produce an optimal schedule. Its greedy choices are not always safe for this objective.

## CODEX correction notes

When analyzing the problem on my own the first time around, I made a couple of misinterpretations. Below is the transparency in how Codex helped me re-understand the problem. Additionally, after the work was done, Codex was also used to help format the document into a much cleaner document.

These notes identify edits to the original draft; they are not part of the counterexample proof.

- **CODEX CORRECTION: Problem setup.** Replaced the original boilerplate's interval/end-time and transit-delay language with ready times, assigned slots, waiting delays, and total value as defined in Task D. Clarified that *every pair* of deliveries has distinct profits.

- **CODEX CORRECTION: Greedy strategy.** Clarified that decreasing profit determines processing order; the earliest available slot is selected separately for each delivery, subject to its ready time.

- **CODEX CORRECTION: Input data.** Kept the revised profit/ready-time data and removed the earlier fixed-interval sample, which belonged to Tasks A–C rather than Task D.

- **CODEX CORRECTION: Calculations.** Added parentheses around 1 + D\_j(S), explicit assigned slots, and the validity check for the alternative schedule.

- **CODEX CORRECTION: Conclusion.** Changed the unsupported claim that the alternative schedule is globally optimal to the claim the assignment requires: it is strictly better than G. The strategy remains greedy, but this example shows it is not always optimal.
