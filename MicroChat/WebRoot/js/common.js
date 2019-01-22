message = {
	"httpAddress":"http://127.0.0.1:8080/MicroChat/"
};
function setIframe(iframe_url, iframe_title) {
	$("#list_content iframe").attr("src", iframe_url);
	$("#title").text(iframe_title);
	sessionStorage.setItem("iframe_url", iframe_url);
	sessionStorage.setItem("iframe_title", iframe_title);
}