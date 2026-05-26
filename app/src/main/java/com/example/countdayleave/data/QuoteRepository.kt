package com.example.countdayleave.data

import java.util.Calendar

object QuoteRepository {
    private val quotes = listOf(
        "Hành trình vạn dặm bắt đầu từ một bước chân.",
        "Đừng đợi cơ hội tự tìm đến, hãy tự tạo ra nó.",
        "Khó khăn không phải để cản bước bạn, mà để rèn luyện bạn vững vàng hơn.",
        "Mỗi ngày là một cơ hội mới để tiến gần hơn đến mục tiêu của bạn.",
        "Thành công không phải là chìa khóa mở cánh cửa hạnh phúc. Hạnh phúc mới là chìa khóa dẫn tới thành công.",
        "Hãy làm những việc khó khăn khi chúng còn dễ dàng và làm những việc vĩ đại khi chúng còn nhỏ bé.",
        "Kỷ luật là cầu nối giữa mục tiêu và thành tựu.",
        "Cách tốt nhất để dự đoán tương lai là tự mình tạo ra nó.",
        "Đừng nhìn lại quá khứ nếu nó làm bạn nản lòng. Hãy nhìn về phía trước để thấy tương lai rộng mở.",
        "Giới hạn duy nhất của chúng ta là những gì chúng ta tự đặt ra trong tâm trí.",
        "Người chiến thắng không bao giờ bỏ cuộc, và người bỏ cuộc không bao giờ chiến thắng.",
        "Thất bại chỉ đơn giản là cơ hội để bắt đầu lại một cách thông minh hơn.",
        "Hãy kiên nhẫn. Những điều tốt đẹp thường cần thời gian để hoàn thiện.",
        "Đừng so sánh mình với người khác. Hãy so sánh mình của ngày hôm nay với chính mình của ngày hôm qua.",
        "Bí quyết của sự thành công là bắt đầu hành động ngay bây giờ.",
        "Ý chí của bạn mới là thứ quyết định bạn có thể đi được bao xa.",
        "Mỗi nỗ lực nhỏ ngày hôm nay sẽ tích lũy thành thành công lớn ngày mai.",
        "Hãy tập trung vào mục tiêu của bạn và đừng để những tiếng ồn xung quanh làm bạn phân tâm.",
        "Khi bạn muốn bỏ cuộc, hãy nhớ lại lý do tại sao bạn đã bắt đầu.",
        "Sự bền bỉ là chìa khóa mở mọi cánh cửa dẫn đến thành công.",
        "Không có gì là không thể với một người luôn biết cố gắng.",
        "Hãy sống như ngày hôm nay là cơ hội cuối cùng để bạn thực hiện ước mơ.",
        "Ước mơ không tự nhiên biến thành hiện thực, nó cần mồ hôi, quyết tâm và sự nỗ lực.",
        "Hãy tin rằng bạn có thể làm được, và bạn đã đi được một nửa chặng đường rồi.",
        "Tập trung vào giải pháp chứ không phải vấn đề.",
        "Năng lượng và sự kiên trì chiến thắng tất cả mọi thứ.",
        "Mỗi ngày trôi qua là một trang sách mới, hãy viết lên đó những điều tuyệt vời nhất.",
        "Chiến thắng bản thân là chiến thắng hiển hách nhất.",
        "Đừng đếm những ngày đã trôi qua, hãy làm cho những ngày trôi qua có ý nghĩa.",
        "Sự chuẩn bị tốt nhất cho ngày mai là làm thật tốt công việc của ngày hôm nay."
    )

    /**
     * Lấy câu quote của ngày hôm nay (thay đổi theo ngày).
     */
    fun getQuoteOfTheDay(): String {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % quotes.size
        return quotes[index]
    }
}
