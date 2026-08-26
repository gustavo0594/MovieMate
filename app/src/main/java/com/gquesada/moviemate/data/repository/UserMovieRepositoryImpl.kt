package com.gquesada.moviemate.data.repository

import com.gquesada.moviemate.data.local.dao.MovieDao
import com.gquesada.moviemate.data.local.dao.UserMovieStateDao
import com.gquesada.moviemate.data.local.entity.UserMovieStateEntity
import com.gquesada.moviemate.data.mapper.toDomain
import com.gquesada.moviemate.domain.model.MovieWithUserState
import com.gquesada.moviemate.domain.model.UserMovieState
import com.gquesada.moviemate.domain.repository.UserMovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserMovieRepositoryImpl(
    private val userMovieStateDao: UserMovieStateDao,
    private val movieDao: MovieDao,
) : UserMovieRepository {

    override fun observeUserState(movieId: Int): Flow<UserMovieState> =
        userMovieStateDao.observeState(movieId).map { it?.toDomain() ?: UserMovieState.empty(movieId) }

    override fun observeFavorites(): Flow<List<MovieWithUserState>> =
        userMovieStateDao.observeFavorites().map { it.toMoviesWithState() }

    override fun observeWatched(): Flow<List<MovieWithUserState>> =
        userMovieStateDao.observeWatched().map { it.toMoviesWithState() }

    override fun observeWatchlist(): Flow<List<MovieWithUserState>> =
        userMovieStateDao.observeWatchlist().map { it.toMoviesWithState() }

    override suspend fun setFavorite(movieId: Int, isFavorite: Boolean) {
        upsert(movieId) { it.copy(isFavorite = isFavorite) }
    }

    override suspend fun setInWatchlist(movieId: Int, inWatchlist: Boolean) {
        upsert(movieId) {
            it.copy(
                isInWatchlist = inWatchlist,
                addedToWatchlistAt = if (inWatchlist) System.currentTimeMillis() else it.addedToWatchlistAt,
            )
        }
    }

    override suspend fun setWatched(movieId: Int, watched: Boolean, personalRating: Int?) {
        upsert(movieId) {
            it.copy(
                isWatched = watched,
                personalRating = if (watched) personalRating else null,
                watchedAt = if (watched) (it.watchedAt ?: System.currentTimeMillis()) else null,
            )
        }
    }

    private suspend fun upsert(movieId: Int, transform: (UserMovieStateEntity) -> UserMovieStateEntity) {
        val current = userMovieStateDao.getState(movieId) ?: UserMovieStateEntity(movieId = movieId)
        userMovieStateDao.upsert(transform(current))
    }

    private suspend fun List<UserMovieStateEntity>.toMoviesWithState(): List<MovieWithUserState> =
        mapNotNull { state ->
            val movie = movieDao.getById(state.movieId) ?: return@mapNotNull null
            MovieWithUserState(movie.toDomain(), state.toDomain())
        }
}
