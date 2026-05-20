# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 1/5 (20.0%)
- **Function parity:** 13/116 matched (target 19) — 11.2%
- **Class/type parity:** 2/25 matched (target 10) — 8.0%
- **Combined symbol parity:** 15/141 matched (target 29) — 10.6%
- **Average inline-code cosine:** 0.45 (function body across 1 matched files)
- **Average documentation cosine:** 0.93 (doc text across 1 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 1 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. base64

- **Target:** `rustlspkitypes.Base64`
- **Similarity:** 0.45
- **Dependents:** 1
- **Priority Score:** 1072205.5
- **Functions:** 13/20 matched (target 19)
- **Missing functions:** `check_models`, `u8_broadcast8_model`, `u8_broadcast16_model`, `u8_nonzero_model`, `u8_equals_model`, `u8_in_range_model`, `codepoint_decode_secret_does_not_branch_or_index_on_secret_input`
- **Types:** 2/2 matched (target 10)
- **Missing types:** _none_
- **Tests:** 2/4 matched

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Next Commands

```bash
# Initialize task queue for systematic porting
cd tools/ast_distance
./ast_distance --init-tasks ../../tmp/rustls-pki-types/src rust ../../src/commonMain/kotlin/io/github/kotlinmania/rustlspkitypes kotlin tasks.json ../../AGENTS.md

# Get next high-priority task
./ast_distance --assign tasks.json <agent-id>
```
## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |

