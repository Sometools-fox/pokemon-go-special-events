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
- **任何 `timeStatus: estimated`／系統暫定日期都屬於需使用者確認的項目**；可先保留為暫定資料供排序與顯示，但處理 Inbox 完成報告必須集中列出讓使用者確認，不得視為已完全確認。

## 圖片判讀信心規則
此規則只適用於圖片／截圖來源，並在「處理 Inbox」時執行。

- 以「欄位或判斷項目」個別評估判讀信心，不以整張圖片給單一信心分數。例如活動名稱、日期、地點、特殊獲取物各自判斷。
- 判讀信心 **80% 以上（含 80%）**：可依圖片內容正常整理，但仍必須遵守「只讀目前來源、不外部補查、不推測來源未提供資訊」。
- 判讀信心 **低於 80%**：不得把該項不確定內容寫入正式 events.json；先列為待使用者確認。
- 完全無法辨識或有多種合理讀法時，同樣視為待確認，不猜測。
- 「來源根本沒有提供」與「圖片有提供但看不清楚」必須區分。前者依一般缺資料規則標記未提供／待確認，不以模型常識或外部資料補足；後者才套用圖片信心規則。
- 信心百分比只用於 Inbox 處理階段判斷是否需要詢問，不寫入正式 events.json。
- 同一 Inbox 圖片有多個低信心項目時，先處理可確定部分，再將所有低於 80% 的問題集中一次詢問使用者，避免逐題中斷。
- 詢問時應列出：待確認欄位／目前可能讀法／約略信心度／可選答案（包含「忽略」）。
- 低信心項目屬「需要人工判斷」，可將該 Inbox 項目暫置 needs-review；但不得阻塞其他可正常處理的 Inbox 項目。
- 使用者確認後，以使用者確認內容為準完成該項；若使用者選擇忽略，該欄位維持未提供／待確認，不自行補值。

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
3. 可安全判斷者建立或更新 V2 活動資料；圖片來源須先套用「圖片判讀信心規則」，低於 80% 的欄位不得直接寫入正式資料。
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

## URL 處理規則
- Inbox 中使用者提供的 URL 必須從 APK／Firebase／GitHub Inbox 到解析、搜尋、processed／needs-review **全流程原樣保存**；完整 URL 是來源主識別值，文章 ID 只能作為輔助搜尋鍵，不得取代原始 URL。
- 凡 Inbox 項目有提供 URL，使用者已長期授權外部查證，不需逐筆再次詢問；原網址可讀時以原頁內容為主要來源。
- 原網址無法直接讀取、內容不足或需要交叉確認時，依序以 **完整原始 URL → 網域＋完整路徑／文章 ID → 可取得的頁面標題／活動識別資訊 → 其他可靠外部來源** 查證。
- **Cache miss ≠ source-unavailable。** Cache miss 只代表該次抓取／快取路徑未取得內容，不得直接判定 URL 非法、文章不存在或來源不可用。
- 只有在重試原 URL、搜尋索引及合理外部查證後，仍不足以可靠辨識基本活動身分，才可標記 source-unavailable／needs-review。
- 外部查證所得資訊必須與原始 URL 直接取得的內容區分，不得偽裝成原頁面內容。
- 沒有 URL 的圖片／純文字項目仍只依使用者提供內容處理，不主動外部補查，除非使用者另行授權。
