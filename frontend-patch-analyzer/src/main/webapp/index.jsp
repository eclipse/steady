<html>
<body>
<form action="pa" method="get">
	Repo URL <input name="u" type="text" value="http://svn.apache.org/repos/asf/cxf/"></br>
	Bug Id <input name="b" type="text" value="CVE-2013-0239"></br>
	Search string  <input name="s" type="text" value=""></br>
	Revision  <input name="r" type="text" value="1438424"></br>
	Delete temp. files <input name="d" type="radio" value="true" checked> Yes <input name="d" type="radio" value="false"> No</br>
	Collector URL <input name="c" type="text" value=""></br>
	Action <select name="a">
		<option value="search">Search commit log</option>
		<option value="identify">Identify changes</option>
		<option value="upload">Upload changes</option>
	</select></br>
	<input type="submit">	
</form>
</body>
</html>
