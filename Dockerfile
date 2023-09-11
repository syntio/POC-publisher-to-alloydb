FROM golang:alpine3.16

# Set environment variables needed for Go runtime
ENV GO111MODULE=on \
CGO_ENABLED=0 \
GOOS=linux \
GOARCH=amd64 \
HOME=/app

WORKDIR $HOME

COPY . .

# change to a non-root user for security
RUN adduser --disabled-password -h $HOME user
USER user

RUN go mod download && go build -o myapp ./pkg/module_example

CMD ["$HOME/myapp"]
