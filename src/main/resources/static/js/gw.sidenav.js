window.sidenavContext = undefined;
GW.sidenav = {

    setSidenavWindowObjects: (
        code, owner, confidentialField, category, codeType, processId, processName
    ) => {
        window.sidenavContext.code = code;
        window.sidenavContext.confidentialField = confidentialField;
        window.sidenavContext.category = category;
        window.sidenavContext.codeType = codeType;
        window.sidenavContext.processId = processId;
        window.sidenavContext.processName = processName;
    },

    getData: (id) => {
        const TYPE = "process";
        let code, owner, confidentialField, category, codeType, processId, processName;

        $.ajax({
            url: "detail",
            method: "POST",
            data: "type=" + TYPE + "&id=" + id
        }).done((response) => {
            let parsedMessage = GW.general.parseResponse(response);
            codeType = parsedMessage.lang.equals(null) ? parsedMessage.description : parsedMessage.lang
            code = parsedMessage.code;
            if (code && code.includes("\\\"")) {
                code = GW.process.unescape(code);
            }
            processId = parsedMessage.id;
            processName = parsedMessage.name;
            owner = parsedMessage.owner;
            confidentialField = parsedMessage.confidential !== "FALSE";

            this.setSidenavWindowObjects(
                code,
                owner,
                confidentialField,
                category,
                codeType,
                processId,
                processName
            )

        })
    }

}