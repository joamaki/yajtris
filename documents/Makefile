# Makefile for YaJTris/documents

#DOCS = specification.tex instructions.tex
DOCS = design.tex usermanual.tex specification.tex implementation.tex

.SUFFIXES:
.SUFFIXES: .tex .pdf

all: $(DOCS:.tex=.pdf)

clean:
	rm -f *.dvi *.log *.aux *.pdf *.toc *.out

.tex.pdf:
# Run pdflatex twice to make sure indexes are correctly generated...
	pdflatex $<
	pdflatex $<


.PHONY : all clean
