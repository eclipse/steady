jQuery.sap.declare("sap.m.sample.TableMergeCells.Formatter");

sap.m.sample.TableMergeCells.Formatter = {

  isEmpty :  function (fValue) {
    try {
      if(fValue=="") {
    	  return true;
      }
      else {
    	  return false;
      }
    } catch (err) {
      return false;
    }
  }
};