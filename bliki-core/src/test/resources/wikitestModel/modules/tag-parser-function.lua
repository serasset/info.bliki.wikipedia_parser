local export = {}
function export.show(frame)
    return mw.getCurrentFrame():extensionTag('b', 'bold')
end
return export