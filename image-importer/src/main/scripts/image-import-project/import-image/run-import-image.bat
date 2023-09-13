For /F "tokens=1* delims==" %%A IN (import-image.properties) DO (
    IF "%%A"=="imageDir" set imageDir=%%B
    IF "%%A"=="projectId" set projectId=%%B
    IF "%%A"=="imageInputSheet" set imageInputSheet=%%B
    IF "%%A"=="assetFolder" set assetFolder=%%B
)


java -jar ./../image_importer.jar -i -s "%imageInputSheet%" -d "%imageDir%" -p "%projectId%" -a "%assetFolder%"
pause