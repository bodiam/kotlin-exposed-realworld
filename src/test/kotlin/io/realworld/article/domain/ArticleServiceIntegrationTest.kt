package io.realworld.article.domain

import io.realworld.article.endpoint.CreateArticleDtoGen
import io.realworld.article.endpoint.UpdateArticleDto
import io.realworld.article.infrastructure.ArticleConfiguration
import io.realworld.shared.Gen
import io.realworld.shared.TestTransactionConfiguration
import io.realworld.test.expectation.Expectation
import io.realworld.test.expectation.ExpectationConfiguration
import io.realworld.test.precondition.Precondition
import io.realworld.test.precondition.PreconditionConfiguration
import io.realworld.user.domain.LoggedUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@SpringBootTest(
        classes = [
            ArticleConfiguration::class,
            PreconditionConfiguration::class,
            ExpectationConfiguration::class,
            TestTransactionConfiguration::class
        ]
)
@ImportAutoConfiguration(
        DataSourceAutoConfiguration::class,
        FlywayAutoConfiguration::class
)
@Transactional
internal class ArticleServiceIntegrationTest {

    @Autowired
    lateinit var articleService: ArticleService

    @Autowired
    lateinit var given: Precondition

    @Autowired
    lateinit var then: Expectation

    lateinit var loggedUser: LoggedUser

    @BeforeEach
    internal fun setUp() {
        loggedUser = given.user.loggedUser()
    }

    @Test
    fun `should save article`() {
        val tagAlpha = TagGen.build()
        val tagBravo = TagGen.build()
        val tagNames = arrayOf(tagAlpha, tagBravo).map(Tag::name)

        given.tag.exists(tagAlpha)

        val createArticleDto = CreateArticleDtoGen.build(tags = tagNames)

        val article = articleService.create(createArticleDto)

        article.run {
            assertThat(title).isEqualTo(createArticleDto.title)
            assertThat(description).isEqualTo(createArticleDto.description)
            assertThat(body).isEqualTo(createArticleDto.body)
            assertThat(tags).extracting<String>(Tag::name).containsExactlyInAnyOrderElementsOf(tagNames)
        }

        then.article.existsFor(article.slug)
    }

    @Test
    fun `should update article`() {
        val article = given.article.exist(ArticleGen.build(author = loggedUser))
        val updateArticleDto = UpdateArticleDto(title = Gen.alphanumeric(20), body = null, description = null)

        val updatedArticleDto = articleService.update(article.slug, updateArticleDto)

        assertThat(updatedArticleDto.title).isEqualTo(updateArticleDto.title)
    }

    @Test
    fun `should delete article`() {
        val article = given.article.exist(ArticleGen.build(author = loggedUser))

        articleService.delete(article.slug)

        then.article.notExistsFor(article.slug)
    }
}
