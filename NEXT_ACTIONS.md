# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/5 (100.0%)
- **Function parity:** 78/109 matched (target 191) — 71.6%
- **Class/type parity:** 33/44 matched (target 67) — 75.0%
- **Combined symbol parity:** 111/153 matched (target 258) — 72.5%
- **Average inline-code cosine:** 0.35 (function body across 5 matched files)
- **Average documentation cosine:** 0.69 (doc text across 5 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 5 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. base64

- **Target:** `rustlspkitypes.Base64`
- **Similarity:** 0.56
- **Dependents:** 1
- **Priority Score:** 1012204.4
- **Functions:** 19/20 matched (target 25)
- **Missing functions:** `codepoint_decode_secret_does_not_branch_or_index_on_secret_input`
- **Types:** 2/2 matched (target 10)
- **Missing types:** _none_
- **Tests:** 3/4 matched

### 2. pem

- **Target:** `rustlspkitypes.Pem`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 153008.2
- **Functions:** 10/22 matched (target 30)
- **Missing functions:** `from_pem_file`, `pem_file_iter`, `from_pem_reader`, `pem_reader_iter`, `from_pem`, `from_buf`, `from_buf_inner`, `read`, `is_end`, `as_ref`, `fmt`, `read_until_newline`
- **Types:** 5/8 matched (target 17)
- **Missing types:** `ReadIter`, `Item`, `Error`

### 3. server_name

- **Target:** `rustlspkitypes.ServerName`
- **Similarity:** 0.37
- **Dependents:** 0
- **Priority Score:** 124906.3
- **Functions:** 28/36 matched (target 66)
- **Missing functions:** `fmt`, `borrow`, `eq`, `hash`, `new`, `parse_with`, `ipv4_address`, `ipv6_address`
- **Types:** 9/13 matched (target 16)
- **Missing types:** `Error`, `DnsNameInner`, `State`, `ReadNumberHelper`
- **Tests:** 10/12 matched

### 4. lib

- **Target:** `rustlspkitypes.Hex`
- **Similarity:** 0.37
- **Dependents:** 0
- **Priority Score:** 114606.3
- **Functions:** 19/27 matched (target 65)
- **Missing functions:** `zeroize`, `from_pem`, `fmt`, `deref`, `config_and_key_from_iter`, `as_secs`, `hash`, `eq`
- **Types:** 16/19 matched (target 23)
- **Missing types:** `Error`, `Target`, `BytesInner`
- **Tests:** 3/3 matched

### 5. alg_id

- **Target:** `rustlspkitypes.AlgorithmIdentifier`
- **Similarity:** 0.25
- **Dependents:** 0
- **Priority Score:** 30607.5
- **Functions:** 2/4 matched (target 5)
- **Missing functions:** `fmt`, `deref`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `Target`

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

