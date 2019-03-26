package com.symeonchen.wakeupscreen.data

enum class Status(val code: Int, val description: String) {
    UNDEFINED(-1, "未定义"),
    LOADING(0, "加载中"),
    OPEN(1, "已开启"),
    CLOSE(2, "已关闭"),
    UNAUTHORIZED(3, "未授权");

    companion object {
        fun fromCode(code: Int): Status {
            for (state in values()) {
                if (state.code == code)
                    return state
            }
            return UNDEFINED
        }
    }

}