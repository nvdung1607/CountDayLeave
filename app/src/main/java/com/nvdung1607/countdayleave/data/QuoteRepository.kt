package com.nvdung1607.countdayleave.data

import android.content.Context
import org.json.JSONArray
import java.util.Calendar

object QuoteRepository {
    private val defaultQuotes = listOf(
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
        "Sự chuẩn bị tốt nhất cho ngày mai là làm thật tốt công việc của ngày hôm nay.",
        "Không bao giờ là quá muộn để bắt đầu xây dựng ước mơ của mình.",
        "Thái độ của bạn quyết định sự cao lớn của bạn.",
        "Cuộc sống không phải là để tìm kiếm bản thân, mà là để tự tạo ra bản thân.",
        "Đừng để ngày hôm qua chiếm quá nhiều thời gian của ngày hôm nay.",
        "Sự khác biệt giữa người thành công và những người khác không phải là sự thiếu sức mạnh, thiếu kiến thức, mà là thiếu ý chí.",
        "Nếu bạn muốn đạt được những điều chưa từng có, hãy sẵn sàng làm những điều chưa từng làm.",
        "Học từ hôm qua, sống cho hôm nay, hy vọng cho ngày mai.",
        "Đừng bao giờ để nỗi sợ thất bại lớn hơn niềm vui chiến thắng.",
        "Không có con đường nào bằng phẳng để dẫn tới thành công, bạn phải tự mở đường cho chính mình.",
        "Hãy tin tưởng vào chính mình, bạn mạnh mẽ hơn bạn nghĩ rất nhiều.",
        "Nơi nào có ý chí, nơi đó có con đường.",
        "Hãy làm việc trong im lặng và để sự thành công của bạn lên tiếng.",
        "Hãy luôn hướng về phía mặt trời, bóng tối sẽ ngả về sau lưng bạn.",
        "Để thành công, khát khao thành công của bạn phải lớn hơn nỗi sợ thất bại.",
        "Mỗi ngày mới mang theo sức mạnh mới và suy nghĩ mới.",
        "Chỉ có những người dám thất bại lớn mới có thể đạt được những thành công lớn.",
        "Đừng cầu nguyện cho một cuộc sống dễ dàng, hãy cầu nguyện cho có sức mạnh để chịu đựng một cuộc sống khó khăn.",
        "Tương lai thuộc về những ai tin vào vẻ đẹp của những giấc mơ.",
        "Thời gian của bạn là có hạn, vì vậy đừng lãng phí nó để sống cuộc đời của người khác.",
        "Thành công là tổng hợp của những nỗ lực nhỏ bé, lặp đi lặp lại ngày này qua ngày khác.",
        "Cách duy nhất để làm một công việc tuyệt vời là yêu thích việc bạn làm.",
        "Hãy tự tin tiến về phía ước mơ của bạn, sống cuộc đời mà bạn hằng mong ước.",
        "Đừng bao giờ từ bỏ ước mơ chỉ vì thời gian để hoàn thành nó quá dài. Thời gian rồi cũng sẽ trôi đi thôi."
    )

    private val quotesList = mutableListOf<String>()

    fun initialize(context: Context) {
        synchronized(this) {
            quotesList.clear()
            val prefs = context.getSharedPreferences("quotes_prefs", Context.MODE_PRIVATE)
            val jsonStr = prefs.getString("custom_quotes", null)
            if (jsonStr != null) {
                try {
                    val arr = JSONArray(jsonStr)
                    for (i in 0 until arr.length()) {
                        quotesList.add(arr.getString(i))
                    }
                } catch (e: Exception) {
                    quotesList.addAll(defaultQuotes)
                }
            } else {
                quotesList.addAll(defaultQuotes)
            }
        }
    }

    private fun save(context: Context) {
        val prefs = context.getSharedPreferences("quotes_prefs", Context.MODE_PRIVATE)
        val arr = JSONArray()
        quotesList.forEach { arr.put(it) }
        prefs.edit().putString("custom_quotes", arr.toString()).apply()
    }

    fun getQuoteOfTheDay(context: Context): String {
        ensureInitialized(context)
        val index = getQuoteOfTheDayIndex(context)
        return getQuote(context, index)
    }

    fun getQuoteOfTheDayIndex(context: Context): Int {
        ensureInitialized(context)
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        return dayOfYear % quotesList.size
    }

    fun getQuote(context: Context, index: Int): String {
        ensureInitialized(context)
        if (quotesList.isEmpty()) return ""
        val safeIndex = ((index % quotesList.size) + quotesList.size) % quotesList.size
        return quotesList[safeIndex]
    }

    fun getQuotesCount(context: Context): Int {
        ensureInitialized(context)
        return quotesList.size
    }

    fun getAllQuotes(context: Context): List<String> {
        ensureInitialized(context)
        return quotesList.toList()
    }

    fun addQuote(context: Context, quote: String) {
        ensureInitialized(context)
        quotesList.add(quote)
        save(context)
    }

    fun editQuote(context: Context, index: Int, newQuote: String) {
        ensureInitialized(context)
        if (index in quotesList.indices) {
            quotesList[index] = newQuote
            save(context)
        }
    }

    fun deleteQuote(context: Context, index: Int) {
        ensureInitialized(context)
        if (index in quotesList.indices) {
            quotesList.removeAt(index)
            if (quotesList.isEmpty()) {
                quotesList.addAll(defaultQuotes)
            }
            save(context)
        }
    }

    private fun ensureInitialized(context: Context) {
        if (quotesList.isEmpty()) {
            initialize(context)
        }
    }
}

