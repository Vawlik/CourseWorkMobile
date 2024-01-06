package com.example.newkursach

interface OnItemClickListener {
    fun onItemClickListener(position: Int)
    fun onItemLongClickListener(position: Int)
    fun onShareClickListener(position: Int)
}