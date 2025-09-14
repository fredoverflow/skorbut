package semantic

import semantic.types.*
import syntax.lexer.TokenKind.*
import syntax.lexer.TokenKindSet

val enumStructUnion = TokenKindSet.of(ENUM, STRUCT, UNION)
val storageClasses = TokenKindSet.of(TYPEDEF, EXTERN, STATIC, AUTO, REGISTER)

val typeSpecifierIdentifier = TokenKindSet.of(IDENTIFIER)

val typeSpecifiers = mapOf(
    TokenKindSet.of(VOID) to VoidType,
    TokenKindSet.of(CHAR) to SignedCharType,
    TokenKindSet.of(SIGNED, CHAR) to SignedCharType,
    TokenKindSet.of(UNSIGNED, CHAR) to UnsignedCharType,
    TokenKindSet.of(SHORT) to SignedShortType,
    TokenKindSet.of(SHORT, INT) to SignedShortType,
    TokenKindSet.of(SIGNED, SHORT) to SignedShortType,
    TokenKindSet.of(SIGNED, SHORT, INT) to SignedShortType,
    TokenKindSet.of(UNSIGNED, SHORT) to UnsignedShortType,
    TokenKindSet.of(UNSIGNED, SHORT, INT) to UnsignedShortType,
    TokenKindSet.of(INT) to SignedIntType,
    TokenKindSet.of(SIGNED) to SignedIntType,
    TokenKindSet.of(SIGNED, INT) to SignedIntType,
    TokenKindSet.of(UNSIGNED) to UnsignedIntType,
    TokenKindSet.of(UNSIGNED, INT) to UnsignedIntType,
    TokenKindSet.of(LONG) to SignedIntType,
    TokenKindSet.of(LONG, INT) to SignedIntType,
    TokenKindSet.of(SIGNED, LONG) to SignedIntType,
    TokenKindSet.of(SIGNED, LONG, INT) to SignedIntType,
    TokenKindSet.of(UNSIGNED, LONG) to UnsignedIntType,
    TokenKindSet.of(UNSIGNED, LONG, INT) to UnsignedIntType,
    TokenKindSet.of(FLOAT) to FloatType,
    TokenKindSet.of(DOUBLE) to DoubleType,
    TokenKindSet.of(ENUM) to Later,
    TokenKindSet.of(STRUCT) to Later,
    typeSpecifierIdentifier to Later,
)
