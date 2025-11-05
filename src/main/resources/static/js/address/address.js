const provinceSelect = document.getElementById('provinceCityCode');
const wardSelect = document.getElementById('communeWardCode');
document.addEventListener('DOMContentLoaded', function () {
  fetch('https://fruitshop.tech/admin/provinces')
    .then(response => {
      if (!response.ok) {
        throw new Error('Lỗi http status: ' + response.status);
      }
      return response.json();
    })
    .then(data => {
      let optionHtml = `<option value="">Chọn tỉnh/thành phố</option>`;
      data.forEach(element => {
        optionHtml += `<option value="${element.code}">${element.name}</option>`;
      });
      provinceSelect.innerHTML = optionHtml;
      wardSelect.innerHTML = `<option value="">Chọn phường/xã</option>`;
      wardSelect.disabled = true;
    });
});

provinceSelect.addEventListener('change', (event) => {
  const provinceId = event.target.value;
  wardSelect.disabled = true; 
  if (provinceId !== "") {
    fetch(`https://fruitshop.tech/admin/wards?provinceId=` + provinceId)
      .then(response => {
        if (!response.ok) {
          throw new Error('Lỗi http status: ' + response.status);
        }
        return response.json();
      })
      .then(data => {
        let optionHtml = '<option value="">Chọn phường/xã</option>';
        data.forEach(element => { 
          optionHtml += `<option value="${element.code}">${element.name}</option>`;
        });
        wardSelect.innerHTML = optionHtml;
      });
  }else{
    wardSelect.innerHTML = `<option value="">Chọn phường/xã</option>`;
  }
  wardSelect.disabled = false; 
});