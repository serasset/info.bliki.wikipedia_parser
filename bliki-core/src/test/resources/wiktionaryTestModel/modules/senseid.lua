local export = {}

function export.etymid()
	return mw.html.create("span")
		:addClass("etymid")
		:attr("id", "English:_" .. "test")

end


return export