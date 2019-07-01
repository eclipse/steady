var b = { 
    named_func : function(anon,x,y) {
        return anon(x,y, function(res){console.log("%f%%", res.toFixed(2));}, function(err){console.error('Error, ' + err);})
    }
};
b.named_func(function(a,b,success,fail) {
        if(a<b) {
            var ans = a/b*100;
            success(ans);
            return ans;
        }
        else {
            fail('Divisor is less than Dividend');
            return null;
        }
    }
    , 11, 20);
b.named_func(function(a,b,success,fail) {
        if(a<b) {
            success(b-a);
            return b-a;
        }
        else {
            fail('b <= a');
            return null;
        }
    }
    , 11, 30);
