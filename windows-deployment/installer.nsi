; Define the name of the installer
OutFile "GeoweaverInstaller.exe"

; Define the installation directory
InstallDir "$PROGRAMFILES\Geoweaver"

; Define the package's properties
Name "Geoweaver"
BrandingText "Geoweaver Installation"
Icon "geoweaver.ico"

; Default installation section
Section "Install Geoweaver"

    ; Set the output path to the installation directory
    SetOutPath $INSTDIR

    ; Include the files from the dist/geoweaver directory
    File /r "D:\a\Geoweaver\Geoweaver\dist\geoweaver\*.*"

    ; Create a desktop shortcut
    CreateShortCut "$DESKTOP\Geoweaver.lnk" "$INSTDIR\geoweaver.exe" "" "$INSTDIR\_internal\geoweaver.ico"

SectionEnd

; Uninstallation section
Section "Uninstall"

    ; Remove the application's files
    RMDir /r $INSTDIR

    ; Remove the desktop shortcut
    Delete "$DESKTOP\Geoweaver.lnk"

SectionEnd
