package de.fabmax.kool.modules.ksl.lang

import de.fabmax.kool.modules.ksl.generator.KslGenerator
import de.fabmax.kool.modules.ksl.model.KslMutatedState

open class KslArrayAccessor<T: KslType>(
    val array: KslExpression<KslTypeArray<T>>,
    val index: KslExpression<KslTypeInt1>)
    : KslExpression<T>, KslAssignable<T> {

    override val expressionType = array.expressionType.elemType
    override val assignType = array.expressionType.elemType

    override val mutatingState: KslValue<*>?
        get() = array as? KslValue<*>

    override fun collectStateDependencies(): Set<KslMutatedState> =
        array.collectStateDependencies() + index.collectStateDependencies()

    override fun generateAssignable(generator: KslGenerator) = generator.arrayValueAssignable(this)
    override fun generateExpression(generator: KslGenerator) = generator.arrayValueExpression(this)
    override fun toPseudoCode() = "${array.toPseudoCode()}[${index.toPseudoCode()}]"
}

class KslArrayScalarAccessor<S>(array: KslScalarArrayExpression<S>, index: KslExpression<KslTypeInt1>) :
    KslArrayAccessor<S>(array, index), KslScalarExpression<S> where S: KslType, S: KslScalar
class KslArrayVectorAccessor<V, S>(array: KslVectorArrayExpression<V, S>, index: KslExpression<KslTypeInt1>) :
    KslArrayAccessor<V>(array, index), KslVectorExpression<V, S> where V: KslType, V: KslVector<S>, S: KslScalar
class KslArrayMatrixAccessor<M, V>(array: KslMatrixArrayExpression<M, V>, index: KslExpression<KslTypeInt1>) :
    KslArrayAccessor<M>(array, index), KslMatrixExpression<M, V> where M: KslType, M : KslMatrix<V>, V: KslVector<*>

operator fun <T: KslType> KslArrayExpression<T>.get(index: Int) =
    KslArrayAccessor(this, KslValueInt1(index))
operator fun <T: KslType> KslArrayExpression<T>.get(index: KslExpression<KslTypeInt1>) =
    KslArrayAccessor(this, index)

operator fun <S> KslScalarArrayExpression<S>.get(index: Int) where S: KslType, S: KslScalar =
    KslArrayScalarAccessor(this, KslValueInt1(index))
operator fun <S> KslScalarArrayExpression<S>.get(index: KslExpression<KslTypeInt1>) where S: KslType, S: KslScalar =
    KslArrayScalarAccessor(this, index)

operator fun <V, S> KslVectorArrayExpression<V, S>.get(index: Int) where V: KslType, V: KslVector<S>, S: KslScalar =
    KslArrayVectorAccessor(this, KslValueInt1(index))
operator fun <V, S> KslVectorArrayExpression<V, S>.get(index: KslExpression<KslTypeInt1>) where V: KslType, V: KslVector<S>, S: KslScalar =
    KslArrayVectorAccessor(this, index)

operator fun <M, V> KslMatrixArrayExpression<M, V>.get(index: Int) where M: KslType, M : KslMatrix<V>, V: KslVector<*> =
    KslArrayMatrixAccessor(this, KslValueInt1(index))
operator fun <M, V> KslMatrixArrayExpression<M, V>.get(index: KslExpression<KslTypeInt1>) where M: KslType, M : KslMatrix<V>, V: KslVector<*> =
    KslArrayMatrixAccessor(this, index)
