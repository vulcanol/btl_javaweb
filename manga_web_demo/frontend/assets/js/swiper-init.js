document.addEventListener('DOMContentLoaded', () => {
    // Khởi tạo Swiper Banner (Bố cục ảnh 1)
    const heroSwiper = new Swiper('.heroSwiper', {
        loop: true,
        autoplay: {
            delay: 4000,
            disableOnInteraction: false,
        },
        pagination: {
            el: '.swiper-pagination',
            clickable: true,
        },
        grabCursor: true
    });

    // Khởi tạo Swiper cho Mới Cập Nhật (1 hàng ngang - Bố cục ảnh 2)
    const updatedSwiper = new Swiper('.updatedSwiper', {
        slidesPerView: 'auto',
        spaceBetween: 16,
        grabCursor: true,
        navigation: {
            nextEl: '.swiper-button-next'
        },
        slidesOffsetAfter: 20
    });
});
