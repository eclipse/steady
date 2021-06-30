var docEl = document.documentElement
var scrollTopEl = document.getElementsByClassName('custom-scroll-top')[0]

function scrollTop() {
    document.body.scrollTop = document.documentElement.scrollTop = 0
}

scrollTopEl.onclick = function () { 
    scrollTop()
}

window.onscroll = function() {
   var sTop = (this.pageYOffset || docEl.scrollTop) - (docEl.clientTop || 0)
   if ( sTop < 100 ) {
       scrollTopEl.classList.add('hidden')
    } else {
        scrollTopEl.classList.remove('hidden')
   }
};