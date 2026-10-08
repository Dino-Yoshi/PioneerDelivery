# 413P — Pioneer Delivery Requirements Handoff

**Date:** 2026-10-08  
**Current stage:** Requirements engineering, before architecture, detailed task SRSs, or code  
**Canonical document:** `SRS-Pioneer-Delivery-v0.2.md`  
**Original assignment:** `CS413-PA-partI-2026Fall.pdf`

## Instructions for the next chat

Continue the CS 413 Pioneer Delivery algorithm portfolio project. **Start with a broad discussion of Tasks A–D together**: what each task is asking at a business/requirements level, how they relate to the overall scheduling system, and where they may share versus require different data or behavior. **Do not yet delve into any one task's detailed requirements, choose algorithm designs, write pseudocode, prescribe architecture, or code.** Ask for the user's reasoning and discuss tradeoffs before formalizing decisions.

The user wants a genuine software-engineering process, not immediate implementation. There will be **one overarching SRS** plus **a separate, linked SRS for each task**. The next chat should first agree on the overarching scope of A–D before producing those task SRSs. The user will explicitly authorize each change of phase.

## What is already agreed

- **Project objectives:** Read delivery requests from a file; determine the maximum compatible deliveries a single driver can handle; determine the minimum drivers needed to serve the accepted requests.
- **Language:** C++17 is preferred, not yet an implementation commitment.
- **Base record:** At least `id`, integer `start_time`, integer `finish_time`, with start strictly less than finish. A shared endpoint is allowed: one delivery may start exactly as another finishes.
- **Time convention (team-chosen):** Nonnegative integer **minute offsets**, with unspecified origin and no application-specific maximum or 24-hour rollover. Minutes and nonnegative restriction are **not** prescribed by the source assignment.
- **Identifiers:** Unique IDs within a dataset; duplicate IDs are invalid. The assignment doesn't require a particular ID pattern.
- **Input file:** Headerless comma-delimited CSV, fields `id,start_time,finish_time`; incidental surrounding whitespace can be trimmed. Input order can be arbitrary.
- **Data integrity:** Keep each ID with its associated interval when sorting. Accepted records should remain unchanged during scheduling; `class` vs. `struct` is not decided.
- **Malformed input:** Do not infer or repair. Skip invalid rows, continue processing valid rows, log the source file, line, reason, ID when readable, and character position when available. Report counts of examined nonblank rows, accepted rows, and skipped rows, and show `See errors.log for details.` (log filename is tentative). Results must be labeled as computed for accepted records only.
- **Empty file:** Valid scenario: report that no deliveries need scheduling; produce zero-size results. A missing/unreadable file remains an error.
- **Testing:** Maintain sample data and test cases; task-specific tests are still to be analyzed.

## Formal documentation state

- Overarching `SRS-Pioneer-Delivery-v0.2.md` contains a Table of Contents, Revision History (v0.1 and v0.2), high-level functional and data requirements, scheduling invariants, input/error policy, engineering decisions, six closed questions, and a deferred-scope section.
- **OQ-01 to OQ-06 are resolved.** The policies are team decisions, not all instructor requirements.
- Detailed Task A–D requirements, output contracts, runtime targets, algorithms, test cases, architectural decomposition, and source code are intentionally absent from the parent SRS.
- No implementation has begun and no detailed design is approved.

## Next discussion goal

Read the assignment's Tasks A–D **at a broad level** to identify their intent, scope boundaries, relationships, and whether any parent-SRS assumptions might need re-evaluation. In particular, avoid assuming every task necessarily shares the base delivery-record format; confirm after reading. Discuss before editing documentation. Later, once the user agrees, establish traceability between the parent SRS and individual task SRSs, then analyze tasks one by one.

**Suggested first question:** “Looking at Tasks A–D together, what do you see as the distinct capabilities Pioneer Delivery is asking us to provide, and which ones seem related?”

## Starting context to send along

Attach or copy `SRS-Pioneer-Delivery-v0.2.md` and the original assignment PDF into the next chat if they are not already accessible in the Project. Treat the v0.2 document as the current requirements baseline; preserve its distinction between explicit requirements and team-selected policies.
