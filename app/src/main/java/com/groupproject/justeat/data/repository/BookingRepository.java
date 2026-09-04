package com.groupproject.justeat.data.repository;

import com.groupproject.justeat.data.model.Booking;

import java.util.List;

/** 订桌数据操作边界，支持顾客和餐厅员工的预订流程。 */
public interface BookingRepository {
    /**
     * 加载某位顾客的全部预订。
     *
     * @param customerId 顾客用户 ID
     * @param callback 返回预订列表或错误
     */
    void getBookingsForCustomer(String customerId,
                                RepositoryCallback<List<Booking>> callback);

    /**
     * 新增或更新预订。
     *
     * @param booking 要保存的预订
     * @param callback 返回已保存预订或错误
     */
    void saveBooking(Booking booking, RepositoryCallback<Booking> callback);

    /**
     * 更新预订状态。
     *
     * @param bookingId 预订 ID
     * @param status 新状态，值来自 BookingStatus
     * @param callback 返回完成状态或错误
     */
    void updateBookingStatus(String bookingId, String status,
                             RepositoryCallback<Void> callback);

    /**
     * 取消指定预订。
     *
     * @param bookingId 预订 ID
     * @param callback 返回完成状态或错误
     */
    void cancelBooking(String bookingId, RepositoryCallback<Void> callback);
}

