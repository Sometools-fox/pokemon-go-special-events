# Event Update Workflow V2

## Intake
只讀使用者本次提供的網址、文字、圖片。除非使用者明確要求，不做外部補查。

## Extract
至少擷取活動名稱、日期、地區/地點、位置資訊、特殊獲取物與來源。缺少資訊使用狀態值，不推測。

## Match
先比對既有資料。同活動有新資訊時沿用原 ID；同系列不同城市/日期場次使用不同 ID；無法確定時不強行合併並標記 unresolved。

## Validate before commit
- ID 不重複，只使用小寫英數與連字號。
- startDate 不晚於 endDate。
- 緯度 -90..90、經度 -180..180。
- lat/lng 同時有值或同時為 null。
- coordinateStatus 與座標存在狀態一致。
- 至少一個 user_provided source。
- createdAt 不因更新改變；updatedAt 設為更新日期。
- 否定資訊（例如「無特殊背卡」）不應視為實際可獲取物，後續整理時移至 notes/details。

## Publish
只更新 data/events.json，GitHub Pages 自動發布；網站不維護第二份活動資料。

## Reply
回報新增/更新活動、日期、地點/GPS 數量、主要特殊獲取物，以及仍未提供的資訊。
