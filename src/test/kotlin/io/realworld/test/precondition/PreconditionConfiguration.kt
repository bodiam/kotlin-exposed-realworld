package io.realworld.test.precondition

import io.realworld.article.infrastructure.SqlArticleWriteRepository
import io.realworld.article.infrastructure.SqlTagWriteRepository
import io.realworld.comment.infrastructure.SqlCommentWriteRepository
import io.realworld.user.infrastructure.SqlUserWriteRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
@ComponentScan("io.realworld.test.precondition")
@Import(value = [
    SqlUserWriteRepository::class, SqlArticleWriteRepository::class, SqlCommentWriteRepository::class, SqlTagWriteRepository::class
])
class PreconditionConfiguration(private val dataSource: DataSource) {
    @Bean
    fun jdbcTemplate() = JdbcTemplate(dataSource)
}
