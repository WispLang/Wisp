add(a: i32, b: i32): i32 {
    -> a + b
}
checkZero(number: i32): u1 {
    number == 0? {
        -> 1
    } :? {
        -> 0
    }
}
testLoops(): i32 {
    num: i32 = 2
    | i: i32 = 1 | i < 15 | i++ | {
        num = num*a
    }
    -> num
}
