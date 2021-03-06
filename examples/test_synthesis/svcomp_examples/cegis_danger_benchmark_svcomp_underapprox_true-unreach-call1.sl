(set-logic BV)

(synth-inv inv-f ((x (BitVec 32))(y (BitVec 32)))
 )

(declare-primed-var x (BitVec 32))

(declare-primed-var y (BitVec 32))

(define-fun pre-f ((x (BitVec 32))(y (BitVec 32))) Bool
    (and (= x #x00000000) (= y #x00000001))
)

(define-fun trans-f ((x (BitVec 32))(y (BitVec 32))(x! (BitVec 32))(y! (BitVec 32))) Bool
    (and (bvult x #x00000006) (and (= x! (bvadd x #x00000001)) (= y! (bvmul y #x00000002))))
)

(define-fun post-f ((x (BitVec 32))(y (BitVec 32))) Bool
    (or (= #x00000001 (bvurem y #x00000003))(bvult x #x00000006))
)

(inv-constraint inv-f pre-f trans-f post-f) 
(check-synth)


