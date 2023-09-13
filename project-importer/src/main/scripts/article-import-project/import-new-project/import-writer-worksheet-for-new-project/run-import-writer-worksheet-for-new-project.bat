For /F "tokens=1* delims==" %%A IN (import-writer-worksheet-for-new-project.properties) DO (
    IF "%%A"=="projectId" set projectId=%%B
    IF "%%A"=="projectSheetPath" set projectSheetPath=%%B
)


java -jar ./../new_project_importer.jar -iw -s "%projectSheetPath%" -p "%projectId%"
pause