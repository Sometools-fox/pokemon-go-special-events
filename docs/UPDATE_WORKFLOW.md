# Event Update Workflow V2

## Intake
只讀使用者本次提供的網址、文字、圖片。除非使用者明確要求，不做外部補查。

## Extract
至少擷取活動名稱、日期、地區/地點、位置資訊、特殊獲取物與來源。缺少資訊使用狀態值，不推測。

### 活動日期缺失的暫定規則
- 若來源完全沒有可用的活動日期，仍可正常入庫：以「本次資料處理日」作為 startDate，endDate 設為 startDate + 6 天，亦即含首日在內共 7 天。
- 此日期必須標記為系統暫定，不可當成來源明確提供的日期；schedule.timeStatus 使用 estimated，timeNote 註明「來源未提供活動日期；系統暫定為收錄日起 7 天」。
- dataQuality.unresolved 必須保留「來源未提供活動日期；目前使用系統 7 天暫定期」。
- 若來源有月／日但缺年份，不使用 7 天暫定期；依「資料處理日」補年份，將來源月／日轉成完整 YYYY-MM-DD。
- 一般情況使用處理日所在年份。例如 2026 年處理「10月4日～10月10日」，寫入 2026-10-04～2026-10-10。
- 跨年判斷：若處理日在年底，而來源日期落在接下來的 1 月或 2 月，視為隔年。例如 2026 年 12 月處理「1月10日」，寫入 2027-01-10。若同一日期範圍由 12 月跨到 1 月，結束日年份亦自動 +1。
- 補上的年份屬系統依收錄時間推定；原始月／日資訊保留在 timeNote，並在 dataQuality.unresolved 註明年份為系統推定。
- 日後同一活動取得來源明確日期時，沿用原活動 ID，以正式日期覆蓋暫定日期並移除相應 unresolved。
- 此規則只補活動日期，不推測時間、地區、GPS 或特殊獲取物。

## Match
先比對既有資料。同活動有新資訊時沿用原 ID；同系列不同城市/日期場次使用不同 ID。

若只是缺少日期、時間、GPS、精確場地或特殊獲取物，仍可正常入庫，不因單純缺資料進入 needs-review。活動日期缺失時依上方「7 天暫定規則」填入可排序日期；其他缺漏依 schema 使用未提供/待確認等狀態。

只有在來源本身無法安全判斷、需要人工決定才進入 needs-review，例如：
- 不確定是否為 Pokémon GO 活動。
- 不確定應新增活動或更新既有活動。
- 日期或時間資訊互相矛盾。
- 地點、座標或來源資訊互相矛盾。
- 網頁無法讀取、需要登入或內容已消失，導致無法建立基本活動身分。
- 活動結構有歧義，無法可靠拆分場次。
- 其他需要人工判斷且不可由目前來源安全決定的情況。

AI 不自行修正疑似錯誤的來源內容；若矛盾會影響正式資料，移至 needs-review。

## Validate before commit
- ID 不重複，只使用小寫英數與連字號。
- startDate 不晚於 endDate。
- 緯度 -90..90、經度 -180..180。
- lat/lng 同時有值或同時為 null。
- coordinateStatus 與座標存在狀態一致。
- 至少一個 user_provided source。
- createdAt 不因更新改變；updatedAt 設為更新日期。
- 否定資訊（例如「無特殊背卡」）不應視為實際可獲取物，後續整理時移至 notes/details。

## Inbox processing
使用者說「處理 Inbox」時：
1. 讀取 inbox/incoming/index.json 與其中列出的 pending 項目。
2. 每筆只使用該筆使用者提供的來源，不主動外部補查。
3. 可安全判斷者建立或更新 V2 活動資料。
4. 完成者移至 inbox/processed/；需要人工判斷者移至 inbox/needs-review/。
5. 單筆 needs-review 不得阻塞其他 Inbox 項目。
6. 刪除已搬移的 incoming 原檔並更新 index。
7. 最後回報處理總數、新增、更新、needs-review、忽略與目前活動總數。

## needs-review
needs-review 是人工判斷隔離區，不是一般缺資料區。

建議 reasonCode：
- source-unavailable
- not-sure-event
- possible-duplicate
- conflicting-date
- conflicting-location
- ambiguous-structure
- insufficient-identity
- other

Review 紀錄應保留原始 Inbox 資料，並增加 review 物件，至少包含 reasonCode、reason；適用時可包含 candidateEventIds 與 suggestedAction。

使用者說「處理 needs-review」時：
1. 讀取所有待 review 項目。
2. 不先修改 events.json；逐項整理真正需要使用者決定的問題與選項。
3. 使用者可選擇新增、更新指定既有活動、忽略、暫緩，或明確授權該項外部查證。
4. 外部查證授權只適用於指定 review 項目，不改變其他活動的來源規則。
5. 解決後，可匯入者更新 events.json 並移至 processed；確認忽略者移至 processed 並記錄 ignored；仍無法確定者留在 needs-review。
6. resolution 應記錄最終處理方式，例如 user_confirmed_new、user_confirmed_update、externally_verified、ignored。

## Publish
正式活動資料只更新 data/events.json，GitHub Pages 自動發布；網站不維護第二份活動資料。

## Reply
回報新增/更新活動、日期、地點/GPS 數量、主要特殊獲取物，以及仍未提供的資訊。處理 Inbox 時另回報 needs-review 與忽略數量。
