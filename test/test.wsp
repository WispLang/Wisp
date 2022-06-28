TestType [
  a: i32
  b: i32
] {
  add(): i32 {
    -> a + b
  }
}

testConditions(number: i32): u1 {
    number == 0? {
        -> 1
    }, number == 1? {
        -> 1
    }, {
        -> 0
    }
}

testLoops(): u1 {
    num: i32 = 2
    [i: i32 = 1, i < 15, i++] {
        num = TestType[i num].add()
    }
    -> testConditions(num)
}