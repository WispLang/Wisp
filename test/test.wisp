<testTypes {
  a: i32
  b: i32
}>

testTypesFunc(val: testTypes): i32 {
    -> val.a + val.b
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
    | i: i32 = 1 | i < 15 | i++ | {
        num = testTypesFunc(<a=i, b=num>)
    }
    -> testConditions(num)
}