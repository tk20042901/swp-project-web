const provinceSelect = document.getElementById('provinceCityCode');
const wardSelect = document.getElementById('communeWardCode');
const saveProvinceCode = document.getElementById('savedProvinceCode'); 
const saveWardCode = document.getElementById('savedWardCode');
document.addEventListener('DOMContentLoaded', function () {
  fetch('https://fruitshop.tech/admin/provinces')
    .then(response => {
      if (!response.ok) {
        throw new Error('Lỗi http status: ' + response.status);
      }
      return response.json();
    })
    .then(data => {
      let optionHtml = ``;
      data.forEach(element => {
        
          optionHtml += `<option value="${element.code}">${element.name}</option>`;
      });
      provinceSelect.innerHTML = optionHtml;
      provinceSelect.value = saveProvinceCode.value;
      wardSelect.innerHTML = ``;
      fetch(`https://fruitshop.tech/admin/wards?provinceId=` + saveProvinceCode.value)
        .then(response => {
          if (!response.ok) {
            throw new Error('Lỗi http status: ' + response.status);
          }
          return response.json();
        })
        .then(data => {
          let optionHtml = '';
          data.forEach(element => { 
            optionHtml += `<option value="${element.code}">${element.name}</option>`;
          });
          wardSelect.innerHTML = optionHtml;
          wardSelect.value = saveWardCode.value;
        });
      wardSelect.disabled = false;
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