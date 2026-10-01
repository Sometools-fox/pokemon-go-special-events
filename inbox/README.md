# Collector Inbox

此目錄是 Android Collector 與正式活動資料之間的隔離層。

- incoming/: 手機同步後的待處理 metadata
- incoming/index.json: 待處理 queue manifest
- processed/: 已完成匯入或確認忽略的紀錄
- needs-review/: 需要人工判斷的隔離區
- attachments 不直接寫入 events.json
- Collector 不得直接修改 data/events.json

## 正常流程
Android → Firebase → GitHub incoming → 使用者說「處理 Inbox」→ ChatGPT 依 V2 規格處理 → processed / needs-review → data/events.json → GitHub Pages。

單筆資料資訊不足不等於 needs-review。來源沒有提供日期細節、GPS、精確場地或特殊獲取物時，依 schema 標記未提供/待確認即可。

只有需要人工做決定、來源互相矛盾、活動身分無法安全判定等情況才進 needs-review。單筆 needs-review 不得阻塞其他 incoming 項目。

## needs-review 紀錄
保留原始 Inbox payload，並加入 review：
- reasonCode
- reason
- candidateEventIds（如適用）
- suggestedAction（如適用）

固定 reasonCode：
- source-unavailable
- not-sure-event
- possible-duplicate
- conflicting-date
- conflicting-location
- ambiguous-structure
- insufficient-identity
- other

使用者說「處理 needs-review」時，先列出需要決定的問題，不直接修改正式活動資料。使用者確認後才新增、更新、忽略或針對指定項目進行外部查證。

## 安全原則
APK 不保存 GitHub PAT。手機只可呼叫受控 Inbox 同步機制。
正式 events.json 仍由 V2 驗證流程更新。
除非使用者明確授權指定項目，活動資料只依使用者提供來源，不主動搜尋外部資料補缺。

## 圖片生命週期
- incoming：Firebase Storage 圖片保留，供處理流程讀取。
- needs-review：圖片持續保留，直到人工完成判斷。
- processed：GitHub Actions 只刪除該 processed metadata 明確引用、且未被 incoming / needs-review 引用的 collector/ 圖片。
- orphan 保險清理：每天檢查 collector/；超過 30 天且未被 incoming / needs-review 引用的物件才可刪除。
- Android 上傳或 Firestore 寫入失敗時，Collector 會嘗試刪除本次已上傳圖片，避免產生新的 orphan。
