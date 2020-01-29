package interpreter

import org.junit.Test

class InterpreterTest {
    private fun run(program: String) {
        Interpreter(program).run()
    }

    @Test fun sum() {
        run("""
int sum(int a, int b)
{
    return a + b;
}

int main()
{
    assert sum(1, 2) == 3;
    return 0;
}
""")
    }

    @Test fun fak() {
        run("""
int fak(int n)
{
    int result;
    if (n == 0)
    {
        result = 1;
    }
    else
    {
        result = fak(n - 1);
        result = n * result;
    }
    return result;
}

int main()
{
    assert fak(5) == 120;
    return 0;
}
""")
    }

    @Test fun passingStrategies() {
        run("""
int pass(int i, int * p)
{
    i = 3;
    *p = 4;
    return i;
}

int main()
{
    int a = 1;
    int b = 2;
    int c = pass(a, &b);
    assert a == 1;
    assert b == 4;
    assert c == 3;
    return 0;
}
""")
    }

    @Test fun doubleResultType() {
        run("""
double the_answer()
{
    return 42.5;
}

int main()
{
    assert the_answer() == 42.5;
    return 0;
}
""")
    }

    @Test fun voidReturnType() {
        run("""
void swap(int * p, int * q)
{
    int c = *p;
    *p = *q;
    *q = c;
}

int main()
{
    int i = 1;
    int k = 2;
    swap(&i, &k);
    assert i == 2;
    assert k == 1;
    return 0;
}
""")
    }

    @Test fun multiDeclaration() {
        run("""
int main()
{
    int *a, b, **c;
    a = &b;
    c = &a;
    return 0;
}
""")
    }

    @Test fun arrayAccess() {
        run("""
int main()
{
    int a[2][3];
    a[0][0] = 2;
    a[0][1] = 3;
    a[0][2] = 5;
    a[1][0] = 7;
    a[1][1] = 11;
    a[1][2] = 13;

    assert a[0][0] == 2;
    assert a[0][1] == 3;
    assert a[0][2] == 5;
    assert a[1][0] == 7;
    assert a[1][1] == 11;
    assert a[1][2] == 13;

    return 0;
}
""")
    }

    @Test fun decay1D() {
        run("""
int main()
{
    int a[2][3];
    a[1][2] = 42;
    int * p = a[1];
    assert p[2] == 42;
    return 0;
}
""")
    }

    @Test fun decay2D() {
        run("""
int main()
{
    int a[2][3];
    a[1][2] = 42;
    int (*p)[3] = a;
    assert p[1][2] == 42;
    return 0;
}
""")
    }

    @Test fun pointerArithmetic() {
        run("""
int main()
{
    int a[9];
    int * p = a + 2;
    int * q = &a[9] - 4;
    assert q - p == 3;
    return 0;
}
""")
    }

    @Test fun charConstants() {
        run("""
int main()
{
    char a = 'a';
    assert 'z' - a == 25;
    return 0;
}
""")
    }

    @Test fun sumOfFirstTenNumbers() {
        run("""
int main()
{
    int sum = 0;
    for (int i = 1; i <= 10; ++i)
    {
        sum = sum + i;
    }
    assert sum == 55;
    return 0;
}
""")
    }

    @Test fun forIntInt() {
        run("""
int main()
{
    int sum = 0;
    for (int a = 1, b = 9; a <= b; ++a, --b)
    {
        sum += a * b;
    }
    assert sum == 1*9 + 2*8 + 3*7 + 4*6 + 5*5;
    return 0;
}
""")
    }

    @Test fun forIntChar() {
        run("""
int main()
{
    char a[7];
    for (struct {int i; char c;} x = {0, 'A'}; x.i < sizeof a; ++x.i, ++x.c)
    {
        a[x.i] = x.c;
    }
    assert a[0] == 'A';
    assert a[1] == 'B';
    assert a[2] == 'C';
    assert a[3] == 'D';
    assert a[4] == 'E';
    assert a[5] == 'F';
    assert a[6] == 'G';
    return 0;
}
""")
    }

    @Test fun forStatic() {
        run("""
void fill(char * p, char * q) {
    for (static char c = 'A'; p < q; ++p) {
        *p = c++;
    }
}

int main()
{
    char a[7];
    fill(a + 0, a + 1);
    fill(a + 1, a + 3);
    fill(a + 3, a + 7);

    assert a[0] == 'A';
    assert a[1] == 'B';
    assert a[2] == 'C';
    assert a[3] == 'D';
    assert a[4] == 'E';
    assert a[5] == 'F';
    assert a[6] == 'G';
    return 0;
}
""")
    }

    @Test fun forTypedef() {
        run("""
int main()
{
    char a[5];
    int i = 0;
    for (typedef const char * str; i < sizeof a; ++i) {
        str p = "world" + i;
        a[i] = *p;
    }
    assert a[0] == 'w';
    assert a[1] == 'o';
    assert a[2] == 'r';
    assert a[3] == 'l';
    assert a[4] == 'd';
    return 0;
}
""")
    }

    @Test fun forScopes() {
        run("""
int main()
{
    int i = 1;
    assert i == 1;
    for (int i = 2; ; ) {
        assert i == 2;

        int i = 3;
        assert i == 3;

        break;
    }
    assert i == 1;
    return 0;
}
""")
    }

    @Test fun gcd() {
        run("""int gcd(int x, int y)
{
    while (y)
    {
        int z = x % y;
        x = y;
        y = z;
    }
    return x;
}

int main()
{
    assert gcd(100, 24) == 4;
    assert gcd(90, 120) == 30;
    assert gcd(13, 99) == 1;
    return 0;
}
""")
    }

    @Test fun twoDimensionalPrimes() {
        run("""
int main()
{
    int primes[2][4] = {{2, 3, 5, 7}, {11, 13, 17, 19}};
    assert primes[0][0] == 2;
    assert primes[0][1] == 3;
    assert primes[0][2] == 5;
    assert primes[0][3] == 7;
    assert primes[1][0] == 11;
    assert primes[1][1] == 13;
    assert primes[1][2] == 17;
    assert primes[1][3] == 19;
    return 0;
}
""")
    }

    @Test fun incompleteArrayParameter() {
        run("""
int sum(int a[], int n)
{
    int s = 0;
    int i;
    for (i = 0; i < n; ++i)
    {
        s = s + a[i];
    }
    return s;
}

int main()
{
    int primes[] = {2, 3, 5, 7, 11, 13, 17, 19};
    assert sum(primes, 8) == 77;
    return 0;
}
""")
    }

    @Test fun bogusSizeArrayParameter() {
        run("""
int sum(int a[5], int n)
{
    int sum = 0;
    int i;
    for (i = 0; i < n; ++i)
    {
        sum = sum + a[i];
    }
    return sum;
}

int main()
{
    int primes[] = {2, 3, 5, 7, 11, 13, 17, 19};
    assert sum(primes, 8) == 77;
    return 0;
}
""")
    }

    @Test fun pointerIncrement() {
        run("""
int main()
{
    char * a = "123";
    char * b = a++;
    char * c = ++a;
    assert *a == '3';
    assert *b == '1';
    assert *c == '3';
    return 0;
}
""")
    }

    @Test fun pointerDecrement() {
        run("""
int main()
{
    char * a = "123" + 2;
    char * b = a--;
    char * c = --a;
    assert *a == '1';
    assert *b == '3';
    assert *c == '1';
    return 0;
}
""")
    }

    @Test fun relationalAndEqualityOnPointers() {
        run("""
int main()
{
    char * a = "hello";
    char * b = a;
    assert a == b;
    assert a <= b;
    assert b >= a;
    ++b;
    assert a != b;
    assert a < b;
    assert a <= b;
    assert b > a;
    assert b >= a;
    return 0;
}
""")
    }

    @Test fun sizeof() {
        run("""
int main()
{
    char c;
    assert sizeof c == 1;
    int i;
    assert sizeof i == 4;
    unsigned u;
    assert sizeof u == 4;
    float f;
    assert sizeof f == 4;
    double d;
    assert sizeof d == 8;
    int a[6];
    assert sizeof a == 24;
    return 0;
}
""")
    }

    @Test fun sizeofType() {
        run("""
int main()
{
    assert sizeof(char) == 1;
    assert sizeof(int) == 4;
    assert sizeof(unsigned) == 4;
    assert sizeof(float) == 4;
    assert sizeof(double) == 8;
    assert sizeof(int[6]) == 24;
    return 0;
}
""")
    }

    @Test fun selfReferentialDeclaration() {
        run("""
int main()
{
    char x;
    {
        int x = sizeof x;
        assert x == sizeof x;
    }
    return 0;
}
""")
    }

    @Test fun sizeOfParen() {
        run("""
int main()
{
    assert sizeof("hello" + 2)[3] == 1;
    return 0;
}
""")
    }

    @Test fun parenthesizedExpression() {
        run("""
int main()
{
    assert 1 + 2 * 3 + 4 == 11;
    assert (1 + 2) * (3 + 4) == 21;
    return 0;
}
""")
    }

    @Test fun comma() {
        run("""
int main()
{
    int a;
    int b;
    int c;
    assert (a = 2, b = 3, c = 5) == 5;
    assert a == 2;
    assert b == 3;
    assert c == 5;
    return 0;
}
""")
    }

    @Test fun logicalNot() {
        run("""
int main()
{
    assert !0 == 1;
    assert !1 == 0;
    assert !2 == 0;
    return 0;
}
""")
    }

    @Test fun logicalAnd() {
        run("""
int main()
{
    assert (0 && 0) == 0;
    assert (0 && 1) == 0;
    assert (1 && 0) == 0;
    assert (1 && 1) == 1;
    return 0;
}
""")
    }

    @Test fun logicalOr() {
        run("""
int main()
{
    assert (0 || 0) == 0;
    assert (0 || 1) == 1;
    assert (1 || 0) == 1;
    assert (1 || 1) == 1;
    return 0;
}
""")
    }

    @Test fun voidPointers() {
        run("""
int main()
{
    int x;
    int * a = &x;
    void * b = a;
    int * c = b;
    assert a == b;
    assert b == c;
    assert a == c;
    return 0;
}
""")
    }

    @Test fun plusMinusAssignment() {
        run("""
int main()
{
    int sum = 0;
    int i;
    for (i = 9; i >= 0; i -= 2)
    {
        sum += i;
    }
    assert sum == 9 + 7 + 5 + 3 + 1;
    return 0;
}
""")
    }

    @Test fun bitwiseAnd() {
        run("""
int main()
{
    assert (1103341801 & 630371112) == 25337896;
    return 0;
}
""")
    }

    @Test fun bitwiseXor() {
        run("""
int main()
{
    assert (1103341801 ^ 630371112) == 1683037121;
    return 0;
}
""")
    }

    @Test fun bitwiseOr() {
        run("""
int main()
{
    assert (1103341801 | 630371112) == 1708375017;
    return 0;
}
""")
    }

    @Test fun doWhile() {
        run("""
int main()
{
    int i = 8;
    do
    {
        ++i;
    } while (i & (i - 1));
    assert i == 16;
    return 0;
}
""")
    }

    @Test fun functionReturningPointerToArray() {
        run("""
char (*f())[6]
{
    return &"hello";
}

int main()
{
    char (*p)[6] = f();
    assert p[0][0] == 'h';
    return 0;
}
""")
    }

    @Test fun rand() {
        run("""
unsigned random;

void srand(unsigned seed)
{
    random = seed;
}

int rand()
{
    random = random * 214013 + 2531011;
    return random * 2 / 131072;
}

int main()
{
    srand(0);
    assert rand() == 38;
    assert rand() == 7719;
    assert rand() == 21238;
    assert rand() == 2437;
    assert rand() == 8855;
    assert rand() == 11797;
    assert rand() == 8365;
    assert rand() == 32285;
    assert rand() == 10450;
    assert rand() == 30612;
    return 0;
}
""")
    }

    @Test fun functionPrototype() {
        run("""
int square(int x);

int main()
{
    assert square(9) == 81;
    return 0;
}

int square(int x)
{
    return x * x;
}
""")
    }

    @Test fun passStructByReference() {
        run("""
struct Point
{
    int x, y;
    int z;
};

void add(struct Point * a, struct Point * b, struct Point * s)
{
    s->x = a->x + b->x;
    s->y = a->y + b->y;
    s->z = a->z + b->z;
}

int main()
{
    struct Point p = {2, 3, 5};
    struct Point q = {7, 11, 13};
    struct Point r;
    add(&p, &q, &r);
    assert r.x == 9;
    assert r.y == 14;
    assert r.z == 18;
    return 0;
}
""")
    }

    @Test fun passFunctionPointer() {
        run("""
int twice(int (*f)(int x), int x)
{
    return f(f(x));
}

int square(int x)
{
    return x * x;
}

int main()
{
    assert twice(square, 3) == 81;
    return 0;
}
""")
    }

    @Test fun indirectMemberAccessViaArray() {
        run("""
int main()
{
    struct Point { int x, y; } a[1];
    a->x = 1;
    a->y = 2;
    assert a[0].x == 1;
    assert a[0].y == 2;
    return 0;
}
""")
    }

    @Test fun unaryPlus() {
        run("""
int abs(int x)
{
    if (x < 0) return -x; else return +x;
}

int main()
{
    assert abs(+42) == 42;
    assert abs(-42) == 42;
    return 0;
}
""")
    }

    @Test fun conditionalArithmetic() {
        run("""
int abs(int x)
{
    return (x < 0) ? -x : +x;
}

int main()
{
    assert abs(+42) == 42;
    assert abs(-42) == 42;
    return 0;
}
""")
    }

    @Test fun conditionalMixedArithmetic() {
        run("""
int main()
{
    double lossy = 1 ? 1234567890 : 3.14f;
    assert lossy == 1234567936;
    return 0;
}
""")
    }

    @Test fun conditionalVoid() {
        run("""
char x;

void True()
{
    x = 't';
}

void False()
{
    x = 'f';
}

int main()
{
    0 ? True() : False();
    assert x == 'f';
    1 ? True() : False();
    assert x == 't';
    return 0;
}
""")
    }

    @Test fun conditionalPointers() {
        run("""
int * minimum(int * p, int * q)
{
    return (*q < *p) ? q : p;
}

int main()
{
    int a[] = {2, 3};
    *minimum(a, a + 1) = 5;
    *minimum(a, a + 1) = 7;
    assert a[0] == 5;
    assert a[1] == 7;
    return 0;
}
""")
    }

    @Test fun conditionalVoidPointers() {
        run("""
int main()
{
    int * a = malloc(sizeof(int));
    void * p = a;
    int * b = malloc(sizeof(int));
    void * q = b;
    free(1 ? p : q);
    free(0 ? p : q);
    return 0;
}
""")
    }

    @Test fun conditionalMixedVoidPointers1() {
        run("""
int main()
{
    int * a = malloc(sizeof(int));
    void * p = a;
    int * q = malloc(sizeof(int));
    free(1 ? p : q);
    free(0 ? p : q);
    return 0;
}
""")
    }

    @Test fun conditionalMixedVoidPointers2() {
        run("""
int main()
{
    int * p = malloc(sizeof(int));
    int * b = malloc(sizeof(int));
    void * q = b;
    free(1 ? p : q);
    free(0 ? p : q);
    return 0;
}
""")
    }

    @Test fun conditionalChained() {
        run("""
int signum(int x)
{
    return x < 0 ? -1 : x > 0 ? +1 : +-0;
}

int main()
{
    assert signum(-42) == -1;
    assert signum(000) == 00;
    assert signum(+42) == +1;
    return 0;
}
""")
    }

    @Test fun forOptional1() {
        run("""
int main()
{
    int sum = 0;
    int i;
    for (i = 1; i <= 10; )
    {
        sum += i;
        ++i;
    }
    assert sum == 55;
    return 0;
}
""")
    }

    @Test fun forOptional2() {
        run("""
int main()
{
    int sum = 0;
    int i;
    for (i = 1; ; ++i)
    {
        if (!(i <= 10))
        {
            assert sum == 55;
            return 0;
        }
        sum += i;
    }
}
""")
    }

    @Test fun forOptional3() {
        run("""
int main()
{
    int sum = 0;
    int i;
    for (i = 1; ; )
    {
        if (!(i <= 10))
        {
            assert sum == 55;
            return 0;
        }
        sum += i;
        ++i;
    }
}
""")
    }

    @Test fun forOptional4() {
        run("""
int main()
{
    int sum = 0;
    int i = 1;
    for (; i <= 10; ++i)
    {
        sum += i;
    }
    assert sum == 55;
    return 0;
}
""")
    }

    @Test fun forOptional5() {
        run("""
int main()
{
    int sum = 0;
    int i = 1;
    for (; i <= 10; )
    {
        sum += i;
        ++i;
    }
    assert sum == 55;
    return 0;
}
""")
    }

    @Test fun forOptional6() {
        run("""
int main()
{
    int sum = 0;
    int i = 1;
    for (; ; ++i)
    {
        if (!(i <= 10))
        {
            assert sum == 55;
            return 0;
        }
        sum += i;
    }
}
""")
    }

    @Test fun forOptional7() {
        run("""
int main()
{
    int sum = 0;
    int i = 1;
    for (; ; )
    {
        if (!(i <= 10))
        {
            assert sum == 55;
            return 0;
        }
        sum += i;
        ++i;
    }
}
""")
    }

    @Test fun mallocSingleElement() {
        run("""
int main()
{
    int * p = malloc(4);
    *p = 42;
    free(p);
    return 0;
}
""")
    }

    @Test fun mallocArray() {
        run("""
int main()
{
    int * p = malloc(12);
    p[0] = 2;
    p[1] = 3;
    p[2] = 5;
    free(p);
    return 0;
}
""")
    }

    @Test fun passMallocResultToFunction() {
        run("""
int * foo(int * p)
{
    *p = 42;
    return p;
}

int main()
{
    free(foo(malloc(4)));
    return 0;
}
""")
    }

    @Test fun typedefScopes() {
        run("""
typedef int X;

X main()
{
    X a = 42;
    {
        X Y = 3;
        X X = 2;
        X * Y;
    }
    X * Y;
    Y = &a;
    char typedef X;
    X * Z;
    Z = "hello";
    return 0;
}
""")
    }

    @Test fun staticLocalCounter() {
        run("""
int a;

int id()
{
    int b;
    static int counter = 0;
    int c;
    return counter++;
}

int d;

int main()
{
    int e;
    assert id() == 0;
    assert id() == 1;
    assert id() == 2;
    return 0;
}
""")
    }

    @Test fun staticLocalStringArray() {
        run("""
char * weekday(int n)
{
    static char * table[] = {
        "Sunday",
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday"
    };
    return table[n];
}

int main()
{
    assert weekday(0) == "Sunday";
    assert weekday(6) == "Saturday";
    return 0;
}
""")
    }

    @Test fun layoutForStaticArrayWithDeducedSize() {
        run("""
int a[] = {1, 2, 3};
int x = 4;

int main()
{
    assert a[0] == 1;
    return 0;
}
""")
    }

    @Test fun signedCharDeclarations() {
        run("""
int main()
{
    signed char a = -1;
    assert a < 0;
    assert sizeof(a) == 1;

    char signed b = -1;
    assert b < 0;
    assert sizeof(b) == 1;

    return 0;
}
""")
    }

    @Test fun unsignedCharDeclarations() {
        run("""
int main()
{
    unsigned char a = -1;
    assert a > 0;
    assert sizeof(a) == 1;

    char unsigned b = -1;
    assert b > 0;
    assert sizeof(b) == 1;

    return 0;
}
""")
    }

    @Test fun signedShortDeclarations() {
        run("""
int main()
{
    short a = -1;
    assert a < 0;
    assert sizeof(a) == 2;


    short int b = -1;
    assert b < 0;
    assert sizeof(b) == 2;

    int short c = -1;
    assert c < 0;
    assert sizeof(c) == 2;


    signed short d = -1;
    assert d < 0;
    assert sizeof(d) == 2;

    short signed e = -1;
    assert e < 0;
    assert sizeof(e) == 2;


    signed short int f = -1;
    assert f < 0;
    assert sizeof(f) == 2;

    signed int short g = -1;
    assert g < 0;
    assert sizeof(g) == 2;

    short signed int h = -1;
    assert h < 0;
    assert sizeof(h) == 2;

    short int signed i = -1;
    assert i < 0;
    assert sizeof(i) == 2;

    int signed short j = -1;
    assert j < 0;
    assert sizeof(j) == 2;

    int short signed k = -1;
    assert k < 0;
    assert sizeof(k) == 2;

    return 0;
}
""")
    }

    @Test fun unsignedShortDeclarations() {
        run("""
int main()
{
    unsigned short d = -1;
    assert d > 0;
    assert sizeof(d) == 2;

    short unsigned e = -1;
    assert e > 0;
    assert sizeof(e) == 2;


    unsigned short int f = -1;
    assert f > 0;
    assert sizeof(f) == 2;

    unsigned int short g = -1;
    assert g > 0;
    assert sizeof(g) == 2;

    short unsigned int h = -1;
    assert h > 0;
    assert sizeof(h) == 2;

    short int unsigned i = -1;
    assert i > 0;
    assert sizeof(i) == 2;

    int unsigned short j = -1;
    assert j > 0;
    assert sizeof(j) == 2;

    int short unsigned k = -1;
    assert k > 0;
    assert sizeof(k) == 2;

    return 0;
}
""")
    }

    @Test fun signedIntDeclarations() {
        run("""
int main()
{
    int a = -1;
    assert a < 0;
    assert sizeof(a) == 4;

    signed b = -1;
    assert b < 0;
    assert sizeof(b) == 4;


    signed int d = -1;
    assert d < 0;
    assert sizeof(d) == 4;

    int signed e = -1;
    assert e < 0;
    assert sizeof(e) == 4;

    return 0;
}
""")
    }

    @Test fun unsignedIntDeclarations() {
        run("""
int main()
{
    unsigned b = -1;
    assert b > 0;
    assert sizeof(b) == 4;


    unsigned int d = -1;
    assert d > 0;
    assert sizeof(d) == 4;

    int unsigned e = -1;
    assert e > 0;
    assert sizeof(e) == 4;

    return 0;
}
""")
    }

    @Test fun typedefAnonymousStruct() {
        run("""
typedef struct
{
    int x, y;
    int z;
} Point;

void add(Point * a, Point * b, Point * s)
{
    s->x = a->x + b->x;
    s->y = a->y + b->y;
    s->z = a->z + b->z;
}

int main()
{
    Point p = {2, 3, 5};
    Point q = {7, 11, 13};
    Point r;
    add(&p, &q, &r);
    assert r.x == 9;
    assert r.y == 14;
    assert r.z == 18;
    return 0;
}
""")
    }

    @Test fun multipleAnonymousStructs() {
        run("""
typedef struct
{
    int x, y;
} Point2D;

typedef struct
{
    int x, y;
    int z;
} Point3D;

int main()
{
    return 0;
}
""")
    }

    @Test fun missingParameterNames() {
        run("""
int square(int);

int twice(int(int), int);

int main()
{
    assert twice(square, 3) == 81;
    return 0;
}

int twice(int f(int), int x)
{
    return f(f(x));
}

int square(int x)
{
    return x * x;
}
""")
    }

    @Test fun trailingCommaInArrayInInitializerList() {
        run("""
int main()
{
    int a[] = {
        2,
        3,
        5,
        7,
    };
    assert sizeof a == 16;
    return 0;
}
""")
    }

    @Test fun anonymousFunctionParameter() {
        run("""
int foo(int (x));

typedef int x;

int bar(int (x));

int main()
{
    sizeof foo(42);
    sizeof bar(foo);
    return 0;
}
""")
    }

    @Test fun parameterShadowsGlobalTypedef() {
        run("""
typedef int x;

void foo(int x)
{
    int y = 1;
    // The following line is now parsed as a multiplication,
    // because x is the int parameter, not the global typedef.
    x * y;
    // Previously, this was a duplicate definition of y (as a pointer to int).
}

int main()
{
    return 0;
}
""")
    }

    @Test fun parameterScopeIsClosed() {
        run("""
typedef int x;

x a;

void foo(int x);

x b;

void foo(int x)
{
}

x c;

int main()
{
    return 0;
}
""")
    }

    @Test fun signedLongDeclarations() {
        run("""
int main()
{
    long a = -1;
    assert a < 0;
    assert sizeof(a) == 4;


    long int b = -1;
    assert b < 0;
    assert sizeof(b) == 4;

    int long c = -1;
    assert c < 0;
    assert sizeof(c) == 4;


    signed long d = -1;
    assert d < 0;
    assert sizeof(d) == 4;

    long signed e = -1;
    assert e < 0;
    assert sizeof(e) == 4;


    signed long int f = -1;
    assert f < 0;
    assert sizeof(f) == 4;

    signed int long g = -1;
    assert g < 0;
    assert sizeof(g) == 4;

    long signed int h = -1;
    assert h < 0;
    assert sizeof(h) == 4;

    long int signed i = -1;
    assert i < 0;
    assert sizeof(i) == 4;

    int signed long j = -1;
    assert j < 0;
    assert sizeof(j) == 4;

    int long signed k = -1;
    assert k < 0;
    assert sizeof(k) == 4;

    return 0;
}
""")
    }

    @Test fun unsignedLongDeclarations() {
        run("""
int main()
{
    unsigned long d = -1;
    assert d > 0;
    assert sizeof(d) == 4;

    long unsigned e = -1;
    assert e > 0;
    assert sizeof(e) == 4;


    unsigned long int f = -1;
    assert f > 0;
    assert sizeof(f) == 4;

    unsigned int long g = -1;
    assert g > 0;
    assert sizeof(g) == 4;

    long unsigned int h = -1;
    assert h > 0;
    assert sizeof(h) == 4;

    long int unsigned i = -1;
    assert i > 0;
    assert sizeof(i) == 4;

    int unsigned long j = -1;
    assert j > 0;
    assert sizeof(j) == 4;

    int long unsigned k = -1;
    assert k > 0;
    assert sizeof(k) == 4;

    return 0;
}
""")
    }

    @Test fun arrayLengthConstantExpression() {
        run("""
int main()
{
    short a[3 * 7];
    assert sizeof a == 42;

    short b[sizeof a / 2];
    assert sizeof b == 42;

    return 0;
}
""")
    }

    @Test fun octalSingleDigit() {
        run("""
int main()
{
    assert 00 == 0;
    assert 01 == 1;
    assert 02 == 2;
    assert 03 == 3;
    assert 04 == 4;
    assert 05 == 5;
    assert 06 == 6;
    assert 07 == 7;

    return 0;
}
""")
    }

    @Test fun octalAllDigits() {
        run("""
int main()
{
    assert 01234567 == 342391;

    return 0;
}
""")
    }

    @Test fun nonOctalFloat() {
        run("""
int main()
{
    assert 010f == 10;

    assert 08f == 8;
    assert 09f == 9;

    return 0;
}
""")
    }

    @Test fun nonOctalDouble() {
        run("""
int main()
{
    assert 010. == 10;

    assert 08. == 8;
    assert 09. == 9;

    return 0;
}
""")
    }

    @Test fun hexSingleDigit() {
        run("""
int main()
{
    assert 0x0 == 0;
    assert 0x1 == 1;
    assert 0x2 == 2;
    assert 0x3 == 3;
    assert 0x4 == 4;
    assert 0x5 == 5;
    assert 0x6 == 6;
    assert 0x7 == 7;
    assert 0x8 == 8;
    assert 0x9 == 9;

    assert 0xA == 10;
    assert 0xB == 11;
    assert 0xC == 12;
    assert 0xD == 13;
    assert 0xE == 14;
    assert 0xF == 15;

    assert 0xa == 10;
    assert 0xb == 11;
    assert 0xc == 12;
    assert 0xd == 13;
    assert 0xe == 14;
    assert 0xf == 15;

    return 0;
}
""")
    }

    @Test fun hexAllDigits() {
        run("""
int main()
{
    assert 0x01234567 == 19088743;

    assert 0x89abcdef == 2309737967;
    assert 0x89ABCDEF == 2309737967;

    return 0;
}
""")
    }

    @Test fun binaryLiterals() {
        run("""
int main()
{
    assert 0b0 == 0;
    assert 0b1 == 1;

    assert 0b10 == 2;
    assert 0b11 == 3;

    assert 0b100 == 4;
    assert 0b101 == 5;
    assert 0b110 == 6;
    assert 0b111 == 7;

    assert 0b1000 == 8;
    assert 0b1001 == 9;
    assert 0b1010 == 10;
    assert 0b1011 == 11;
    assert 0b1100 == 12;
    assert 0b1101 == 13;
    assert 0b1110 == 14;
    assert 0b1111 == 15;

    assert 0b01001001100101100000001011010010 == 1234567890;
    assert 0b10001011110100000011100000110101 == 2345678901;

    return 0;
}
""")
    }

    @Test fun localFunctionPrototype() {
        run("""
int main()
{
    int square(int);
    assert square(9) == 81;
    return 0;
}

int square(int x)
{
    return x * x;
}
""")
    }

    @Test fun enumUninitialized() {
        run("""
enum dir { north, east, south, west }
sun = east;

int main()
{
    enum dir x = 0;
    assert x == north;

    ++x;
    assert x == sun;

    ++x;
    assert x == south;

    ++x;
    assert x == west;

    return 0;
}
""")
    }

    @Test fun enumInitialized() {
        run("""
enum { Y = 6 };

int main()
{
    enum { X = 7 };
    char a[Y][X];
    assert sizeof a == 42;
    return 0;
}
""")
    }

    @Test fun enumMixed() {
        run("""
enum numbers {
    a = 4,
    b = 8,
    c = 15,
    d,
    e = 23,
    f = 42
};

int main()
{
    assert a == 4;
    assert b == 8;
    assert c == 15;
    assert d == 16;
    assert e == 23;
    assert f == 42;

    return 0;
}
""")
    }

    @Test fun staticInitializationArithmetic() {
        run("""
char a;
unsigned char b;
short c;
unsigned short d;
int e;
unsigned int f;
float g;
double h;

int main()
{
    assert a == 0;
    assert b == 0;
    assert c == 0;
    assert d == 0;
    assert e == 0;
    assert f == 0;
    assert g == 0;
    assert h == 0;

    return 0;
}
""")
    }

    @Test fun staticInitializationArray() {
        run("""
int a[5];

int main()
{
    assert a[0] == 0;
    assert a[1] == 0;
    assert a[2] == 0;
    assert a[3] == 0;
    assert a[4] == 0;

    return 0;
}
""")
    }

    @Test fun staticInitializationStruct() {
        run("""
struct Point
{
    int x, y, z;
} p;

int main()
{
    assert p.x == 0;
    assert p.y == 0;
    assert p.z == 0;

    return 0;
}
""")
    }

    @Test fun staticInitializationNested() {
        run("""
struct Person
{
    char name[20];
    int age;
} a[2];

int main()
{
    assert a[0].name[0] == 0;
    assert a[0].name[19] == 0;
    assert a[0].age == 0;

    assert a[1].name[0] == 0;
    assert a[1].name[19] == 0;
    assert a[1].age == 0;

    return 0;
}
""")
    }

    @Test fun staticInitializationLocal() {
        run("""
int a;

int main()
{
    int b = 42;
    static int c;

    assert a == 0;
    assert c == 0;

    return 0;
}
""")
    }

    @Test fun functionPointersAreConstantExpressions() {
        run("""
int square(int x)
{
    return x * x;
}

int (*fp)(int) = &square;

int main()
{
    assert fp(3) == 9;

    return 0;
}
""")
    }

    @Test fun signedUnsignedComparisons() {
        run("""
int main()
{
    assert -1 == 0xffffffff;
    unsigned z = 0;
    assert z < -1;
    assert z <= -1;
    assert -1 > z;
    assert -1 >= z;
    return 0;
}
""")
    }

    @Test fun realloc() {
        run("""
int main()
{
    int * p = malloc(12);
    p[0] = 2;
    p[1] = 3;
    p[2] = 5;

    p = realloc(p, 16);
    p[3] = 7;

    assert p[0] == 2;
    assert p[1] == 3;
    assert p[2] == 5;
    assert p[3] == 7;

    free(p);
    return 0;
}
""")
    }

    @Test fun simpleContinue() {
        run("""
int main()
{
    int x = 0;
    int i = 0;
    for (i = 0; i <= 12; ++i)
    {
        if (i % 2 == 0) continue;
        if (i % 3 == 0) continue;
        x += i;
    }
    assert(x == 1+5+7+11);
    return 0;
}
""")
    }

    @Test fun innerContinue() {
        run("""
int main()
{
    int n = 0;
    int i = 0;
    for (i = 1; i <= 10; ++i)
    {
        int j = 0;
        for (j = 1; j <= 10; ++j)
        {
            if (i == j) continue;
            ++n;
        }
    }
    assert(n == 100 - 10);
    return 0;
}
""")
    }

    @Test fun outerContinue() {
        run("""
int main()
{
    int n = 0;
    int i = 0;
    for (i = 1; i <= 10; ++i)
    {
        if (i % 2 == 0) continue;
        while (0)
        {
            --i;
        }
        ++n;
    }
    assert(n == 5);
    return 0;
}
""")
    }

    @Test fun simpleBreak() {
        run("""
int main()
{
    int n = 0;
    int i = 0;
    for (i = 1; i <= 10; ++i)
    {
        ++n;
        if (i == 5) break;
        ++n;
    }
    assert(n == 5 + 4);
    return 0;
}
""")
    }

    @Test fun simpleGoto() {
        run("""
int main()
{
    int x = 0;
    int y = 0;
    int z = 0;

    ++x;
    goto increment_z;
    ++y;
increment_z:
    ++z;

    assert x == 1;
    assert y == 0;
    assert z == 1;
    return 0;
}
""")
    }

    @Test fun simulateDoWhileWithGoto() {
        run("""
int main()
{
    int sum = 0;
    int i = 1;

loop:
    sum += i;
    ++i;
    if (i <= 10) goto loop;

    assert sum == 55;
    return 0;
}
""")
    }

    @Test fun fallthrough() {
        run("""
int log10(int x)
{
    int log = 0;
    switch (x)
    {
        case 1000000000: ++log;
        case 100000000: ++log;
        case 10000000: ++log;
        case 1000000: ++log;
        case 100000: ++log;
        case 10000: ++log;
        case 1000: ++log;
        case 100: ++log;
        case 10: ++log;
        case 1: return log;
        default: return -1;
    }
}

int main()
{
    assert 0 == log10(1);
    assert 1 == log10(10);
    assert 2 == log10(100);
    assert 3 == log10(1000);
    assert 4 == log10(10000);
    assert 5 == log10(100000);
    assert 6 == log10(1000000);
    assert 7 == log10(10000000);
    assert 8 == log10(100000000);
    assert 9 == log10(1000000000);
    return 0;
}
""")
    }

    @Test fun defaultAboveOthers() {
        run("""
int isPrime(int x)
{
    switch (x)
    {
        default: return 0;
        case 2:
        case 3:
        case 5:
        case 7: return 1;
    }
}

int main()
{
    assert!isPrime(0);
    assert!isPrime(1);
    assert isPrime(2);
    assert isPrime(3);
    assert!isPrime(4);
    assert isPrime(5);
    assert!isPrime(6);
    assert isPrime(7);
    assert!isPrime(8);
    assert!isPrime(9);
    return 0;
}
""")
    }

    @Test fun missingBreak() {
        run("""
const char * color(int x)
{
    const char * result = "";
    switch (x)
    {
        case 0:
        result = "red";
        break;

        case 1:
        result = "green";
        // missing break

        case 2:
        result = "blue";
        break;
    }
    return result;
}

int main()
{
    assert color(0) == "red";
    assert color(1) == "blue";   // due to missing break
    assert color(2) == "blue";
    assert color(3) == "";
    return 0;
}
""")
    }

    @Test fun duffsDevice() {
        run("""
void duffsDevice(char * dst, const char * src, unsigned n)
{
    unsigned rest = n % 8;
    n = n / 8;
    switch (rest)
    {
        case 0: do { *dst++ = *src++;
        case 7:      *dst++ = *src++;
        case 6:      *dst++ = *src++;
        case 5:      *dst++ = *src++;
        case 4:      *dst++ = *src++;
        case 3:      *dst++ = *src++;
        case 2:      *dst++ = *src++;
        case 1:      *dst++ = *src++;
                } while (n--);
    }
}

int main()
{
    char a[] = "................";
    duffsDevice(a, "0123456789", 10);
    assert a[0] == '0';
    assert a[1] == '1';
    assert a[2] == '2';
    assert a[3] == '3';
    assert a[4] == '4';
    assert a[5] == '5';
    assert a[6] == '6';
    assert a[7] == '7';
    assert a[8] == '8';
    assert a[9] == '9';
    assert a[10] == '.';
    return 0;
}
""")
    }

    @Test fun bitwiseNot() {
        run("""
int main()
{
    int i = 0;
    i = ~i;
    assert i == -1;

    unsigned u = 0;
    u = ~u;
    assert u == 4294967295;

    assert sizeof(~'a') == sizeof(int);
    assert ~'a' == -98;

    return 0;
}
""")
    }

    @Test fun signedShift() {
        run("""
int main()
{
    int x = 5;
    x = x << 3;
    assert x == 40;

    x = x >> 2;
    assert x == 10;

    assert -1 >> 1 == -1;

    return 0;
}
""")
    }

    @Test fun unsignedShift() {
        run("""
int main()
{
    assert 0x80000000 >> 1 == 0x40000000;
    assert 0x80000000 << 1 == 0;
    return 0;
}
""")
    }

    @Test fun voidParameterList() {
        run("""
int main(void)
{
    assert sizeof main() == sizeof(int);
    return 0;
}
""")
    }

    @Test fun bsearchPresentElements() {
        run("""
int less(const void * x, const void * y)
{
    const int * p = x;
    const int * q = y;
    int result = (*p < *q) ? -1 : (*p > *q);
    return result;
}

int main(void)
{
    int a[] = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29};
    int i;
    for (i = 0; i < 10; ++i)
    {
        assert bsearch(a + i, a, 10, 4, less) == a + i;
    }
    return 0;
}
""")
    }

    @Test fun bsearchAbsentElements() {
        run("""
int less(const void * x, const void * y)
{
    const int * p = x;
    const int * q = y;
    int result = (*p < *q) ? -1 : (*p > *q);
    return result;
}

int main(void)
{
    int a[] = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29};
    int b[] = {0, 1, 4, 6, 8, 9, 10, 12, 14, 15, 16, 18, 20, 21, 22, 24, 25, 26, 27, 28, 30};
    int i;
    for (i = 0; i < 21; ++i)
    {
        assert bsearch(b + i, a, 10, 4, less) == a + 10;
    }
    return 0;
}
""")
    }

    @Test fun localTypedefBackReference() {
        run("""
int main()
{
    typedef double a, b, c[sizeof(a)], d[sizeof(b)];

    assert sizeof(a) == 8;
    assert sizeof(b) == 8;
    assert sizeof(c) == 64;
    assert sizeof(d) == 64;

    return 0;
}
""")
    }

    @Test fun globalTypedefBackReference() {
        run("""
typedef double a, b, c[sizeof(a)], d[sizeof(b)];

int main()
{
    assert sizeof(a) == 8;
    assert sizeof(b) == 8;
    assert sizeof(c) == 64;
    assert sizeof(d) == 64;

    return 0;
}
""")
    }

    @Test fun postfixAfterSizeof() {
        run("""
int main()
{
    char x = 'a';
    assert sizeof(x)++ == 1;
    assert sizeof x ++ == 1;
    return 0;
}
""")
    }

    @Test fun conditionalBindsStrongerThanComma() {
        run("""
int main()
{
    assert (1 ? 2 : 3, 4) == 4;
    return 0;
}
""")
    }

    @Test fun initEnumeratorWithSizeof() {
        run("""
enum { N = sizeof(int) };

int main()
{
    assert N == 4;
    return 0;
}
""")
    }
}
