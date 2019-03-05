package semantic

import freditor.persistent.ChampMap
import semantic.types.*
import syntax.lexer.TokenKind.*
import syntax.lexer.TokenKindSet

val typeSpecifierIdentifier = TokenKindSet.of(IDENTIFIER)

val typeSpecifiers = ChampMap.empty<TokenKindSet, Type>()
        .put(TokenKindSet.of(VOID), VoidType)
        .put(TokenKindSet.of(CHAR), SignedCharType)
        .put(TokenKindSet.of(SIGNED, CHAR), SignedCharType)
        .put(TokenKindSet.of(UNSIGNED, CHAR), UnsignedCharType)
        .put(TokenKindSet.of(SHORT), SignedShortType)
        .put(TokenKindSet.of(SHORT, INT), SignedShortType)
        .put(TokenKindSet.of(SIGNED, SHORT), SignedShortType)
        .put(TokenKindSet.of(SIGNED, SHORT, INT), SignedShortType)
        .put(TokenKindSet.of(UNSIGNED, SHORT), UnsignedShortType)
        .put(TokenKindSet.of(UNSIGNED, SHORT, INT), UnsignedShortType)
        .put(TokenKindSet.of(INT), SignedIntType)
        .put(TokenKindSet.of(SIGNED), SignedIntType)
        .put(TokenKindSet.of(SIGNED, INT), SignedIntType)
        .put(TokenKindSet.of(UNSIGNED), UnsignedIntType)
        .put(TokenKindSet.of(UNSIGNED, INT), UnsignedIntType)
        .put(TokenKindSet.of(LONG), SignedIntType)
        .put(TokenKindSet.of(LONG, INT), SignedIntType)
        .put(TokenKindSet.of(SIGNED, LONG), SignedIntType)
        .put(TokenKindSet.of(SIGNED, LONG, INT), SignedIntType)
        .put(TokenKindSet.of(UNSIGNED, LONG), UnsignedIntType)
        .put(TokenKindSet.of(UNSIGNED, LONG, INT), UnsignedIntType)
        .put(TokenKindSet.of(FLOAT), FloatType)
        .put(TokenKindSet.of(DOUBLE), DoubleType)
        .put(TokenKindSet.of(ENUM), Later)
        .put(TokenKindSet.of(STRUCT), Later)
        .put(typeSpecifierIdentifier, Later)
