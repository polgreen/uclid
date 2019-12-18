all: semantics.pdf

semantics.pdf: *.tex 
	pdflatex -halt-on-error semantics.tex
	bibtex semantics
	pdflatex -halt-on-error semantics.tex
	pdflatex -halt-on-error semantics.tex
		
.PHONY: clean
	

clean:
	rm -f semantics.ps semantics.pdf *.dvi *.bbl *.aux *.log *.blg *.out

open:
	if which okular > /dev/null; then okular cc17.pdf; else open cc17.pdf; fi
