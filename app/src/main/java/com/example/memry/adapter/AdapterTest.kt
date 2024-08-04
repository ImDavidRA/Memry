import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memry.R
import com.example.memry.dataClasses.Audios
import java.io.IOException

class AdapterTest(private val audioList: List<Audios>) : RecyclerView.Adapter<AdapterTest.MyViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingPosition: Int = -1

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
        holder.durationTextView.text = currentAudio.duration

        // Set initial visibility of icons
        holder.playIcon.visibility = if (position == currentPlayingPosition && mediaPlayer?.isPlaying == true) View.GONE else View.VISIBLE
        holder.pauseIcon.visibility = if (position == currentPlayingPosition && mediaPlayer?.isPlaying == true) View.VISIBLE else View.GONE

        holder.playIcon.setOnClickListener {
            if (mediaPlayer == null) {
                // Create and start a new MediaPlayer instance
                mediaPlayer = MediaPlayer().apply {
                    try {
                        setDataSource(currentAudio.output)
                        prepare()
                        start()
                        holder.playIcon.visibility = View.GONE
                        holder.pauseIcon.visibility = View.VISIBLE
                        currentPlayingPosition = position
                        setOnCompletionListener {
                            holder.playIcon.visibility = View.VISIBLE
                            holder.pauseIcon.visibility = View.GONE
                            mediaPlayer?.release()
                            mediaPlayer = null
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else if (position == currentPlayingPosition) {
                // Toggle play/pause for the current audio
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    holder.playIcon.visibility = View.VISIBLE
                    holder.pauseIcon.visibility = View.GONE
                } else {
                    mediaPlayer?.start()
                    holder.playIcon.visibility = View.GONE
                    holder.pauseIcon.visibility = View.VISIBLE
                }
            } else {
                // Stop current audio and start a new one
                mediaPlayer?.release()
                mediaPlayer = null
                notifyItemChanged(currentPlayingPosition)
                mediaPlayer = MediaPlayer().apply {
                    try {
                        setDataSource(currentAudio.output)
                        prepare()
                        start()
                        holder.playIcon.visibility = View.GONE
                        holder.pauseIcon.visibility = View.VISIBLE
                        currentPlayingPosition = position
                        setOnCompletionListener {
                            holder.playIcon.visibility = View.VISIBLE
                            holder.pauseIcon.visibility = View.GONE
                            mediaPlayer?.release()
                            mediaPlayer = null
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        holder.pauseIcon.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                holder.playIcon.visibility = View.VISIBLE
                holder.pauseIcon.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        super.onViewRecycled(holder)
        // Release MediaPlayer if it's no longer used
        if (currentPlayingPosition == holder.adapterPosition) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}
