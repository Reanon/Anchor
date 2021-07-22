package com.reanon.community;

import com.reanon.community.dao.DiscussPostMapper;
import com.reanon.community.dao.elasticsearch.DiscussPostRepository;
import com.reanon.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 测试 Elasticsearch
 *
 * @author reanon
 * @create 2021-07-11
 */
@SpringBootTest
public class ElasticsearchTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    // 注意不要注入ElasticsearchTemplate
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    @DisplayName("插入数据到 Elasticsearch")
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    @DisplayName("插入多条数据")
    public void testInsertList() {
        for (int uid = 0; uid < 160; uid++) {
            discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(uid, 0, 200, 0));
        }
    }

    @Test
    @DisplayName("es 里更新数据")
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("alice 这里是");
        // 重新save即可
        discussPostRepository.save(post);
    }

    @Test
    @DisplayName("es 里删除数据")
    public void testDelete() {
        // 删除所有数据
        // discussPostRepository.deleteAll();
        discussPostRepository.deleteById(231);
    }

    @Test
    @DisplayName("es 查询并高亮关键词")
    public void testSearchByRepository() {
        // 构造 NativeSearchQuery 接口实现类
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                // 查询条件: 指定搜索字段和关键词
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // 排序条件: type -> score -> createTime
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 分页条件
                .withPageable(PageRequest.of(0, 10))
                // 高亮显示
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

       // elasticsearchRestTemplate.queryForPage(searchQuery,class,searchResultMapper);
       // discussPostRepository 底层获取到了高亮的值, 但没有返回
       // 因此只能用 ElasticsearchRestTemplate
        Page<DiscussPost> page = discussPostRepository.search(nativeSearchQuery);
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (DiscussPost discussPost : page) {
            System.out.println(discussPost);
        }
    }

    @Test
    @DisplayName("高亮查询")
    public void testSearchByTemplate() {
        // 构造 NativeSearchQuery 接口实现类
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                // 查询条件: 指定搜索字段和关键词
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // 排序条件: type -> score -> createTime
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 分页条件
                .withPageable(PageRequest.of(0, 10))
                // 高亮显示
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        SearchHits<DiscussPost> search = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        // 得到查询结果返回的内容
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        // 设置一个需要返回的实体类集合
        List<DiscussPost> discussPosts = new ArrayList<>();
        for (SearchHit<DiscussPost> searchHit : searchHits) {
            // 高亮的内容
            Map<String, List<String>> highLightFields = searchHit.getHighlightFields();
            // 将高亮的内容填充到 content 中
            searchHit.getContent().setTitle(highLightFields.get("title") == null ? searchHit.getContent().getTitle()
                    : highLightFields.get("title").get(0));
            searchHit.getContent().setTitle(highLightFields.get("content") == null ?
                    searchHit.getContent().getContent() : highLightFields.get("content").get(0));
            // 放到实体类中
            discussPosts.add(searchHit.getContent());
        }
        // 输出查询到的包含关键字的条数
        System.out.println(discussPosts.size());
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
    }
}
