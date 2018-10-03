(set-option :fixedpoint.engine pdr)

(declare-rel init (Int Int))
(declare-rel tr (Int Int))
(declare-rel inv (Int Int))
(declare-var A Int)
(declare-var B Int)

(rule (! (=> (and (>= A 0) (>= B 0)) (init A B)) :named initRule))
(rule (! (=> (tr A B) (tr (+ A 1) (+ B A))) :named trRule))
(rule (! (=> (init A B) (tr A B)) :named initTrRule))
(rule (! (=> (init A B) (inv A B)) :named initInvRule))
(rule (! (=> (tr A B) (inv A B)) :named trInvRule))

(declare-rel prop (Int Int))
(rule (=> (and (inv A B) (< B 0)) (prop A B))) 
(query prop :print-certificate true)
