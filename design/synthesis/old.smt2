(declare-fun initial_b () Int)
(declare-fun initial_a () Int)
(declare-fun new_a () Int)
(declare-fun new_b () Int)

(assert 
(or 
    (<= 0 1)
    (and 
        (<= initial_a initial_b)
        (= new_a initial_b)
        (= new_b (+ initial_a initial_b))
        (not (<= new_a new_b)))))

(check-sat)