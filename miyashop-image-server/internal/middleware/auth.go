package middleware

import (
	"fmt"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"

	"miyashop-image-server/internal/model"
)

// AuthMiddleware validates JWT tokens from the Authorization header.
// It extracts claims and stores them in the Gin context.
func AuthMiddleware(secret string) gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" || !strings.HasPrefix(authHeader, "Bearer ") {
			c.JSON(http.StatusUnauthorized, model.Error(model.CodeTokenInvalid, "Token无效"))
			c.Abort()
			return
		}

		tokenString := strings.TrimPrefix(authHeader, "Bearer ")

		token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
			// Validate the signing algorithm matches the Java backend (HMAC-SHA256)
			if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
			}
			return []byte(secret), nil
		})

		if err != nil {
			c.JSON(http.StatusUnauthorized, model.Error(model.CodeTokenInvalid, "Token无效"))
			c.Abort()
			return
		}

		claims, ok := token.Claims.(jwt.MapClaims)
		if !ok || !token.Valid {
			c.JSON(http.StatusUnauthorized, model.Error(model.CodeTokenInvalid, "Token无效"))
			c.Abort()
			return
		}

		// Store claims in context for downstream handlers
		c.Set("userId", claims["userId"])
		c.Set("username", claims["username"])
		c.Set("role", claims["role"])
		c.Set("sub", claims["sub"])
		c.Next()
	}
}

// AdminMiddleware checks that the authenticated user has the ADMIN role.
// Must be used after AuthMiddleware.
func AdminMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		role, exists := c.Get("role")
		if !exists {
			c.JSON(http.StatusForbidden, model.Error(model.CodeForbidden, "无权限访问"))
			c.Abort()
			return
		}

		roleStr, ok := role.(string)
		if !ok || roleStr != "ADMIN" {
			c.JSON(http.StatusForbidden, model.Error(model.CodeForbidden, "无权限访问"))
			c.Abort()
			return
		}

		c.Next()
	}
}
