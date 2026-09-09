# Grade eMMC — Google Lens Style + Web Fallback

Final intended flow:

1. Open Scan eMMC.
2. Live camera with tap-to-focus, vertical large zoom controls and flash.
3. Press FOTO / SCAN TULISAN.
4. Run OCR on the captured photo only.
5. Show detected text in a selectable/editable result screen.
6. COPY copies the selected/full OCR text.
7. SEARCH first checks the local eMMC database.
8. If no matching eMMC code exists locally, open a web search for the cleaned
   OCR text / part number.
9. Web result is reference data only. User can edit Manufacturer, Capacity,
   Grade and eMMC Version before saving it to the local database.

Important:
- No "Target IC" overlay.
- Grade remains a classification of eMMC, not a damage status.
- Capacity remains a first-class field.
- Web data is never silently saved as fact; user reviews it first.
