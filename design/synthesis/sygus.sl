(synth-fun h ((x Int) (y Int)) Bool)

(declare-var initial_b Int)
(declare-var initial_a Int)
(declare-var new_a Int)
(declare-var new_b Int)

(constraint (not 
(or 
    (not (h 0 1))
    (and 
        (and (<= initial_a initial_b) (h initial_a initial_b))
        (= new_a initial_b)
        (= new_b (+ initial_a initial_b))
        (not (and (<= new_a new_b) (h new_a new_b)))))))

(check-synth)
