package com.example.memry.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memry.R
import com.example.memry.dataClasses.Audios
import com.example.memry.helpers.Grabadora

class AdapterTest(private val audioList: List<Audios>, private val context: Context) : RecyclerView.Adapter<AdapterTest.MyViewHolder>() {

    private var currentPlayingPosition: Int = -1
    private var grabadora: Grabadora = Grabadora(context)

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
        val playIcon: ImageView = itemView.findViewById(R.id.playimg)
        val pauseIcon: ImageView = itemView.findViewById(R.id.pauseimg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_best_deal, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentAudio = audioList[position]
        holder.titleTextView.text = currentAudio.title

        // Formateo del texto de la duración
        val durationSecs = currentAudio.duration
        val minutes = durationSecs / 60
        val seconds = durationSecs % 60
        val formattedDuration = String.format("%d:%02d", minutes, seconds)
        holder.durationTextView.text = formattedDuration

        // Inicializar visibilidad basada en el estado del MediaPlayer
        if (position == currentPlayingPosition && grabadora.get_media_player()?.isPlaying == true) {
            holder.playIcon.visibility = View.GONE
            holder.pauseIcon.visibility = View.VISIBLE
        } else {
            holder.playIcon.visibility = View.VISIBLE
            holder.pauseIcon.visibility = View.GONE
        }

        holder.playIcon.setOnClickListener {
            if (grabadora.get_media_player() == null) {
                grabadora.playAudio(currentAudio.output) {
                    holder.playIcon.visibility = View.VISIBLE
                    holder.pauseIcon.visibility = View.GONE
                    grabadora.set_media_player(null)
                }
                holder.playIcon.visibility = View.GONE
                holder.pauseIcon.visibility = View.VISIBLE
                currentPlayingPosition = position
            } else if (position == currentPlayingPosition) {
                // Toggle play/pause for the current audio
                val mediaPlayer = grabadora.get_media_player()
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                        holder.playIcon.visibility = View.VISIBLE
                        holder.pauseIcon.visibility = View.GONE
                    } else {
                        mediaPlayer.start()
                        holder.playIcon.visibility = View.GONE
                        holder.pauseIcon.visibility = View.VISIBLE
                    }
                }
            } else {
                // Stop current audio and start a new one
                grabadora.get_media_player()?.let { mediaPlayer ->
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.stop()
                    }
                    mediaPlayer.release()
                }
                grabadora.set_media_player(null)
                notifyItemChanged(currentPlayingPosition)
                grabadora.playAudio(currentAudio.output) {
                    holder.playIcon.visibility = View.VISIBLE
                    holder.pauseIcon.visibility = View.GONE
                    grabadora.set_media_player(null)
                }
                holder.playIcon.visibility = View.GONE
                holder.pauseIcon.visibility = View.VISIBLE
                currentPlayingPosition = position
            }
        }

        holder.pauseIcon.setOnClickListener {
            grabadora.pausar_audio()
            // Actualiza la visibilidad cuando se pausa el audio
            holder.playIcon.visibility = View.VISIBLE
            holder.pauseIcon.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        super.onViewRecycled(holder)
        // Verifica si el MediaPlayer está en uso y solo libera si es necesario
        if (currentPlayingPosition == holder.adapterPosition) {
            grabadora.get_media_player()?.let { mediaPlayer ->
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
                grabadora.set_media_player(null)
            }
        }
    }
}
