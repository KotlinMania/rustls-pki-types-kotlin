# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 4/5 (80.0%)
- **Function parity:** 80/82 matched (target 165) — 97.6%
- **Class/type parity:** 23/25 matched (target 54) — 92.0%
- **Combined symbol parity:** 103/107 matched (target 219) — 96.3%
- **Average inline-code cosine:** 0.51 (function body across 4 matched files)
- **Average documentation cosine:** 0.86 (doc text across 4 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 4 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. base64

- **Target:** `rustlspkitypes.Base64`
- **Similarity:** 0.57
- **Dependents:** 1
- **Priority Score:** 1012204.3
- **Functions:** 20/20 matched (target 26)
- **Missing functions:** _none_
- **Types:** 1/2 matched (target 10)
- **Missing types:** `Error`
- **Tests:** 4/4 matched

### 2. server_name

- **Target:** `rustlspkitypes.ServerName`
- **Similarity:** 0.45
- **Dependents:** 0
- **Priority Score:** 34905.5
- **Functions:** 34/36 matched (target 84)
- **Missing functions:** `ipv4_address`, `ipv6_address`
- **Types:** 12/13 matched (target 20)
- **Missing types:** `Error`
- **Tests:** 10/12 matched

### 3. pem

- **Target:** `rustlspkitypes.Pem`
- **Similarity:** 0.46
- **Dependents:** 0
- **Priority Score:** 3005.4
- **Functions:** 22/22 matched (target 48)
- **Missing functions:** _none_
- **Types:** 8/8 matched (target 22)
- **Missing types:** _none_

### 4. alg_id

- **Target:** `rustlspkitypes.AlgorithmIdentifier`
- **Similarity:** 0.54
- **Dependents:** 0
- **Priority Score:** 604.6
- **Functions:** 4/4 matched (target 7)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |

