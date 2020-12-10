package com.bullfrog.layoutmanagerdemo.data

class Item(val text: String) {
    companion object {
        val data: MutableList<Item> by lazy {
            prepareData()
        }

        private fun prepareData(): MutableList<Item> {
            val data = mutableListOf<Item>()
            repeat(40) {
                val item = Item("item $it")
                data.add(item)
            }
            return data
        }
    }
}
