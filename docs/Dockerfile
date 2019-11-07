# Image built on top of python:3.6.8-alpine3.9
# with git, (pip) Markdown, mkdocs, mkdocs-material, pip
FROM squidfunk/mkdocs-material:4.4.3

WORKDIR /tmp
COPY . .

RUN pip install -r requirements.txt
EXPOSE 8000

ENTRYPOINT ["python", "docs.py"]
CMD ["public", "--mkserve", "--dev_addr", "docs:8000"]
