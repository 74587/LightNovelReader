package io.nightfish.lightnovelreader.api.content.component

abstract class AbstractDivisibleContentComponent<T: AbstractContentComponent<Data>, Data: AbstractContentComponentData>(
    data: Data
): AbstractContentComponent<Data>(data) {
    abstract fun split(height: Int, width: Int): List<T>
}